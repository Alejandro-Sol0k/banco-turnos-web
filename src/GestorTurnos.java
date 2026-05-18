public class GestorTurnos {
    private final ColaBanco colaBanco;
    private Cliente clienteActual;

    public GestorTurnos() {
        this.colaBanco = new ColaBanco();
        this.clienteActual = null;
    }

    public Cliente registrar(String nombre, TipoDocumento tipoDocumento, String documento, int edad) {
        return colaBanco.registrarCliente(nombre, tipoDocumento, documento, edad);
    }

    public Cliente llamarSiguiente() {
        clienteActual = colaBanco.llamarSiguiente();
        return clienteActual;
    }

    public Cliente getClienteActual() {
        return clienteActual;
    }

    public int getPendientes() {
        return colaBanco.getPendientes();
    }

    public int getAtendidos() {
        return colaBanco.getAtendidos();
    }

    public Integer getProximoTurno() {
        return colaBanco.getProximoTurno();
    }

    public void recorrerHistorial(Visitante<Cliente> visitante) {
        colaBanco.recorrerHistorial(visitante);
    }

    public void recorrerEspera(Visitante<Cliente> visitante) {
        colaBanco.recorrerEnEspera(visitante);
    }

    public double getTiempoPromedioEsperaSegundos() {
        return colaBanco.getTiempoPromedioEsperaSegundos();
    }

    public Cliente buscarPorDocumento(TipoDocumento tipoDocumento, String documento) {
        return colaBanco.buscarPorDocumento(tipoDocumento, documento);
    }

    public boolean cancelarTurno(TipoDocumento tipoDocumento, String documento) {
        return colaBanco.cancelarTurno(tipoDocumento, documento);
    }

    public void reiniciarSistema() {
        clienteActual = null;
        colaBanco.reiniciar();
    }
}
