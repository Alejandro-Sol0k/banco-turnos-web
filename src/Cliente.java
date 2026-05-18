public class Cliente implements Comparable<Cliente> {
    private final String nombre;
    private final TipoDocumento tipoDocumento;
    private final String documento;
    private final int edad;
    private final TipoCliente tipo;
    private final int turno;
    private final long horaRegistro;

    public Cliente(String nombre, TipoDocumento tipoDocumento, String documento, int edad, TipoCliente tipo, int turno, long horaRegistro) {
        this.nombre = nombre;
        this.tipoDocumento = tipoDocumento;
        this.documento = documento;
        this.edad = edad;
        this.tipo = tipo;
        this.turno = turno;
        this.horaRegistro = horaRegistro;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public String getDocumento() {
        return documento;
    }

    public int getEdad() {
        return edad;
    }

    public TipoCliente getTipo() {
        return tipo;
    }

    public int getTurno() {
        return turno;
    }

    public long getHoraRegistro() {
        return horaRegistro;
    }

    @Override
    public int compareTo(Cliente other) {
        // Menor turno primero para prioridad absoluta entre adultos mayores
        return Integer.compare(this.turno, other.turno);
    }

    @Override
    public String toString() {
        return String.format("Turno %d - %s (%s %s) - Edad %d - %s", turno, nombre, tipoDocumento, documento, edad, tipo);
    }
}
