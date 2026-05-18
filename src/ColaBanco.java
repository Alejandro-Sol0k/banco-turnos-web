public class ColaBanco {
    private final Cola<Cliente> colaNormal;
    private final Cola<Cliente> colaPrioridad;
    private final Cola<Cliente> historialAtendidos;
    private int turnosGenerados;
    private long totalEsperaMillis;

    public ColaBanco() {
        this.colaNormal = new Cola<>();
        this.colaPrioridad = new Cola<>();
        this.historialAtendidos = new Cola<>();
        this.turnosGenerados = 0;
        this.totalEsperaMillis = 0L;
    }

    public Cliente registrarCliente(String nombre, TipoDocumento tipoDocumento, String documento, int edad) {
        String nombreNormalizado = nombre == null ? "" : nombre.trim();
        String docNormalizado = documento == null ? "" : documento.trim();

        if (nombreNormalizado.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (!esNombreValido(nombreNormalizado)) {
            throw new IllegalArgumentException("El nombre debe tener entre 2 y 60 caracteres.");
        }
        if (tipoDocumento == null) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio.");
        }
        if (docNormalizado.isEmpty()) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
        if (!esDocumentoValido(docNormalizado)) {
            throw new IllegalArgumentException("El documento debe ser numerico (6-12 digitos).");
        }
        if (edad < 18 || edad > 120) {
            throw new IllegalArgumentException("La edad debe estar entre 18 y 120.");
        }

        TipoCliente tipo = edad >= 60 ? TipoCliente.ADULTO_MAYOR : TipoCliente.NORMAL;
        int turno = ++turnosGenerados;
        long horaRegistro = System.currentTimeMillis();
        Cliente cliente = new Cliente(nombreNormalizado, tipoDocumento, docNormalizado, edad, tipo, turno, horaRegistro);

        if (tipo == TipoCliente.ADULTO_MAYOR) {
            colaPrioridad.insertar(cliente);
        } else {
            colaNormal.insertar(cliente);
        }

        return cliente;
    }

    public void cargarCliente(Cliente cliente) {
        if (cliente == null) {
            return;
        }
        if (cliente.getTipo() == TipoCliente.ADULTO_MAYOR) {
            colaPrioridad.insertar(cliente);
        } else {
            colaNormal.insertar(cliente);
        }
        if (cliente.getTurno() > turnosGenerados) {
            turnosGenerados = cliente.getTurno();
        }
    }

    public void sincronizarTurno(int ultimoTurno) {
        if (ultimoTurno > turnosGenerados) {
            turnosGenerados = ultimoTurno;
        }
    }

    public Cliente llamarSiguiente() {
        Cliente siguiente;
        if (!colaPrioridad.vacia()) {
            siguiente = colaPrioridad.extraer();
        } else {
            siguiente = colaNormal.extraer();
        }

        if (siguiente != null) {
            long espera = System.currentTimeMillis() - siguiente.getHoraRegistro();
            if (espera > 0) {
                totalEsperaMillis += espera;
            }
            historialAtendidos.insertar(siguiente);
        }

        return siguiente;
    }

    public int getPendientes() {
        return colaNormal.tamanio() + colaPrioridad.tamanio();
    }

    public int getAtendidos() {
        return historialAtendidos.tamanio();
    }

    public Integer getProximoTurno() {
    Cliente siguiente = colaPrioridad.cabeza();

    if (siguiente == null) {
        siguiente = colaNormal.cabeza();
    }

    return siguiente == null ? null : siguiente.getTurno();
}

    public void recorrerHistorial(Visitante<Cliente> visitante) {
        historialAtendidos.recorrer(visitante);
    }

    public void recorrerEnEspera(Visitante<Cliente> visitante) {
        colaPrioridad.recorrer(visitante);
        colaNormal.recorrer(visitante);
    }

    public double getTiempoPromedioEsperaSegundos() {
        if (historialAtendidos.tamanio() == 0) {
            return 0.0;
        }
        return (totalEsperaMillis / 1000.0) / historialAtendidos.tamanio();
    }

    public Cliente buscarPorDocumento(TipoDocumento tipoDocumento, String documento) {
        if (tipoDocumento == null || documento == null || documento.trim().isEmpty()) {
            return null;
        }
        String doc = documento.trim();

        Cliente encontrado = buscarEnCola(colaPrioridad, tipoDocumento, doc);
        if (encontrado != null) {
            return encontrado;
        }
        return buscarEnCola(colaNormal, tipoDocumento, doc);
    }

    public boolean cancelarTurno(TipoDocumento tipoDocumento, String documento) {
        if (tipoDocumento == null || documento == null || documento.trim().isEmpty()) {
            return false;
        }
        String doc = documento.trim();

        if (removerDeCola(colaPrioridad, tipoDocumento, doc)) {
            return true;
        }
        return removerDeCola(colaNormal, tipoDocumento, doc);
    }

    public void reiniciar() {
        colaNormal.clear();
        colaPrioridad.clear();
        historialAtendidos.clear();
        turnosGenerados = 0;
        totalEsperaMillis = 0L;
    }

    private Cliente buscarEnCola(Cola<Cliente> cola, TipoDocumento tipoDocumento, String documento) {
        final Caja<Cliente> caja = new Caja<>();
        cola.recorrer(new Visitante<Cliente>() {
            @Override
            public void aceptar(Cliente cliente) {
                if (caja.valor == null
                    && cliente.getTipoDocumento() == tipoDocumento
                    && cliente.getDocumento().equalsIgnoreCase(documento)) {
                    caja.valor = cliente;
                }
            }
        });
        if (caja.valor != null) {
            return caja.valor;
        }
        return null;
    }

    private boolean removerDeCola(Cola<Cliente> cola, TipoDocumento tipoDocumento, String documento) {
        int total = cola.tamanio();
        boolean removido = false;
        for (int i = 0; i < total; i++) {
            Cliente actual = cola.extraer();
            if (!removido && actual != null
                && actual.getTipoDocumento() == tipoDocumento
                && actual.getDocumento().equalsIgnoreCase(documento)) {
                removido = true;
            } else if (actual != null) {
                cola.insertar(actual);
            }
        }
        return removido;
    }

    private boolean esNombreValido(String nombre) {
        if (nombre == null) {
            return false;
        }
        int largo = nombre.length();
        return largo >= 2 && largo <= 60;
    }

    private boolean esDocumentoValido(String documento) {
        if (documento == null) {
            return false;
        }
        int largo = documento.length();
        if (largo < 6 || largo > 12) {
            return false;
        }
        for (int i = 0; i < largo; i++) {
            char c = documento.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private static class Caja<T> {
        private T valor;
    }
}
