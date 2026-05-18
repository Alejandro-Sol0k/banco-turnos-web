public class BancoService {
    private final ColaBanco colaBanco;
    private final BancoRepository repository;

    public BancoService(BancoRepository repository) {
        this.repository = repository;
        this.colaBanco = new ColaBanco();
        cargarPendientes();
    }

    private void cargarPendientes() {
        repository.recorrerPendientes(new Visitante<Cliente>() {
            @Override
            public void aceptar(Cliente cliente) {
                colaBanco.cargarCliente(cliente);
            }
        });
        colaBanco.sincronizarTurno(repository.obtenerUltimoTurno());
    }

    public Cliente registrar(String nombre, TipoDocumento tipoDocumento, String documento, int edad) {
    Cliente cliente = colaBanco.registrarCliente(nombre, tipoDocumento, documento, edad);

    try {
        repository.guardarCliente(cliente);
        return cliente;
    } catch (Exception ex) {
        throw new IllegalArgumentException(
            "El turno ya fue asignado. Intente nuevamente."
        );
    }
}

    public Cliente atenderSiguiente() {
        Cliente cliente = colaBanco.llamarSiguiente();
        if (cliente != null) {
            repository.actualizarEstado(cliente.getTipoDocumento(), cliente.getDocumento(), "ATENDIDO");
        }
        return cliente;
    }

    public boolean cancelar(TipoDocumento tipoDocumento, String documento) {
        boolean cancelado = colaBanco.cancelarTurno(tipoDocumento, documento);
        if (cancelado) {
            repository.actualizarEstado(tipoDocumento, documento, "CANCELADO");
        }
        return cancelado;
    }

    public int getAtendidos() {
        return repository.contarPorEstado("ATENDIDO");
    }

    public int getPendientes() {
        return repository.contarPorEstado("EN_ESPERA");
    }

    public Integer getProximoTurno() {
        return colaBanco.getProximoTurno();
    }

    public double getTiempoPromedioEsperaSegundos() {
        return colaBanco.getTiempoPromedioEsperaSegundos();
    }

    public void recorrerCola(Visitante<Cliente> visitante) {
        colaBanco.recorrerEnEspera(visitante);
    }

    public void recorrerHistorial(Visitante<Cliente> visitante) {
        repository.recorrerHistorial(visitante);
    }

    public Cliente getUltimoAtendido() {
        return repository.obtenerUltimoAtendido();
    }
}
