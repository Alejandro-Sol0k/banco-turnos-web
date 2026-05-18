import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
public class BancoRepository {
    private final DbConfig config;

    public BancoRepository(DbConfig config) {
        this.config = config;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getJdbcUrl(), config.getUser(), config.getPassword());
    }

    public void guardarCliente(Cliente cliente) {
        String sql = "INSERT INTO clientes (nombre, tipo_documento, documento, edad, tipo_cliente, turno, estado) "
            + "VALUES (?, ?, ?, ?, ?, ?, 'EN_ESPERA')";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getTipoDocumento().name());
            ps.setString(3, cliente.getDocumento());
            ps.setInt(4, cliente.getEdad());
            ps.setString(5, cliente.getTipo().name());
            ps.setInt(6, cliente.getTurno());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error guardando cliente: " + ex.getMessage());
        }
    }

    public void recorrerPendientes(Visitante<Cliente> visitante) {
        String sql = "SELECT nombre, tipo_documento, documento, edad, tipo_cliente, turno, hora_registro "
            + "FROM clientes WHERE estado = 'EN_ESPERA' ORDER BY turno";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                visitante.aceptar(mapCliente(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error cargando pendientes: " + ex.getMessage());
        }
    }

    public void recorrerHistorial(Visitante<Cliente> visitante) {
        String sql = "SELECT nombre, tipo_documento, documento, edad, tipo_cliente, turno, hora_registro "
            + "FROM clientes WHERE estado = 'ATENDIDO' ORDER BY turno";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                visitante.aceptar(mapCliente(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error cargando historial: " + ex.getMessage());
        }
    }

    public Cliente obtenerUltimoAtendido() {
        String sql = "SELECT nombre, tipo_documento, documento, edad, tipo_cliente, turno, hora_registro "
            + "FROM clientes WHERE estado = 'ATENDIDO' ORDER BY turno DESC LIMIT 1";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapCliente(rs);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error consultando ultimo atendido: " + ex.getMessage());
        }
        return null;
    }

    public int obtenerUltimoTurno() {
        String sql = "SELECT COALESCE(MAX(turno), 0) AS ultimo FROM clientes";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("ultimo");
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error consultando turnos: " + ex.getMessage());
        }
        return 0;
    }

    public int contarPorEstado(String estado) {
        String sql = "SELECT COUNT(*) AS total FROM clientes WHERE estado = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error contando estado: " + ex.getMessage());
        }
        return 0;
    }

    public void actualizarEstado(TipoDocumento tipoDocumento, String documento, String estado) {
        String sql = "UPDATE clientes SET estado = ? WHERE tipo_documento = ? AND documento = ? AND estado = 'EN_ESPERA'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setString(2, tipoDocumento.name());
            ps.setString(3, documento);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error actualizando estado: " + ex.getMessage());
        }
    }

    private Cliente mapCliente(ResultSet rs) throws SQLException {
        String nombre = rs.getString("nombre");
        TipoDocumento tipoDocumento = TipoDocumento.valueOf(rs.getString("tipo_documento"));
        String documento = rs.getString("documento");
        int edad = rs.getInt("edad");
        TipoCliente tipoCliente = TipoCliente.valueOf(rs.getString("tipo_cliente"));
        int turno = rs.getInt("turno");
        Timestamp hora = rs.getTimestamp("hora_registro");
        long horaRegistro = hora == null ? System.currentTimeMillis() : hora.getTime();
        return new Cliente(nombre, tipoDocumento, documento, edad, tipoCliente, turno, horaRegistro);
    }
}
