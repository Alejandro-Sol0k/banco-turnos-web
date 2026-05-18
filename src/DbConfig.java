import java.util.Map;

public class DbConfig {
    private final String host;
    private final int port;
    private final String name;
    private final String user;
    private final String password;
    private final boolean ssl;

    public DbConfig(String host, int port, String name, String user, String password, boolean ssl) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.user = user;
        this.password = password;
        this.ssl = ssl;
    }

    public static DbConfig fromEnv(Map<String, String> env) {
        String host = env.getOrDefault("DB_HOST", "localhost");
        int port = Integer.parseInt(env.getOrDefault("DB_PORT", "5432"));
        String name = env.getOrDefault("DB_NAME", "banco_turnos");
        String user = env.getOrDefault("DB_USER", "postgres");
        String password = env.getOrDefault("DB_PASSWORD", "");
        boolean ssl = Boolean.parseBoolean(env.getOrDefault("DB_SSL", "false"));
        return new DbConfig(host, port, name, user, password, ssl);
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getJdbcUrl() {
        return "jdbc:postgresql://" + host + ":" + port + "/" + name + "?ssl=" + ssl;
    }
}
