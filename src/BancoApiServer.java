import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class BancoApiServer {
    private final BancoService service;

    public BancoApiServer(BancoService service) {
        this.service = service;
    }

    public static void main(String[] args) throws IOException {
        // 1. Cargamos las variables del archivo .env local si existe
        Map<String, String> env = EnvLoader.load(".env");
        
        // 2. ¡EL TRUCO! Si no están en el archivo, las buscamos en el sistema (Para Render)
        if (!env.containsKey("DB_HOST") && System.getenv("DB_HOST") != null) {
            env.put("DB_HOST", System.getenv("DB_HOST"));
            env.put("DB_PORT", System.getenv("DB_PORT"));
            env.put("DB_NAME", System.getenv("DB_NAME"));
            env.put("DB_USER", System.getenv("DB_USER"));
            env.put("DB_PASSWORD", System.getenv("DB_PASSWORD"));
            env.put("DB_SSL", System.getenv("DB_SSL"));
        }
        
        // Lo mismo para el puerto de la aplicación web
        if (!env.containsKey("APP_PORT") && System.getenv("APP_PORT") != null) {
            env.put("APP_PORT", System.getenv("APP_PORT"));
        }

        DbConfig config = DbConfig.fromEnv(env);
        BancoRepository repo = new BancoRepository(config);
        BancoService service = new BancoService(repo);

        int port = Integer.parseInt(env.getOrDefault("APP_PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        BancoApiServer api = new BancoApiServer(service);

        // Registro único de endpoints del API bancaria
        server.createContext("/api/registrar", api::handleRegistrar);
        server.createContext("/api/atender", api::handleAtender);
        server.createContext("/api/estado", api::handleEstado);
        server.createContext("/api/cancelar", api::handleCancelar);
        server.createContext("/api/cola", api::handleCola);
        server.createContext("/api/historial", api::handleHistorial);
        server.createContext("/api/actual", api::handleActual);
        server.createContext("/", new StaticHandler());

        server.start();
        System.out.println("Servidor bancario interconectado corriendo en el puerto " + port);
    }

    private void handleRegistrar(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"ok\":false,\"mensaje\":\"Metodo no permitido\"}");
            return;
        }
        Map<String, String> data = HttpUtils.parseForm(HttpUtils.readBody(exchange.getRequestBody()));
        try {
            String nombre = data.get("nombre");
            String documento = data.get("documento");
            int edad = Integer.parseInt(data.getOrDefault("edad", "0"));
            TipoDocumento tipoDocumento = TipoDocumento.valueOf(data.getOrDefault("tipoDocumento", ""));

            Cliente cliente = service.registrar(nombre, tipoDocumento, documento, edad);
            String json = "{\"ok\":true,\"cliente\":" + toJson(cliente) + "}";
            sendJson(exchange, 200, json);
        } catch (Exception ex) {
            String json = "{\"ok\":false,\"mensaje\":\"" + HttpUtils.escapeJson(ex.getMessage()) + "\"}";
            sendJson(exchange, 400, json);
        }
    }

    private void handleAtender(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"ok\":false,\"mensaje\":\"Metodo no permitido\"}");
            return;
        }
        Cliente cliente = service.atenderSiguiente();
        String json = cliente == null
            ? "{\"ok\":true,\"cliente\":null}"
            : "{\"ok\":true,\"cliente\":" + toJson(cliente) + "}";
        sendJson(exchange, 200, json);
    }

    private void handleEstado(HttpExchange exchange) throws IOException {
        int atendidos = service.getAtendidos();
        int pendientes = service.getPendientes();
        Integer proximo = service.getProximoTurno();
        double promedio = service.getTiempoPromedioEsperaSegundos();
        String json = "{\"ok\":true,\"atendidos\":" + atendidos + ",\"pendientes\":" + pendientes
    + ",\"proximoTurno\":" + (proximo == null ? "null" : proximo)
    + ",\"promedioEspera\":" + String.format(java.util.Locale.US, "%.2f", promedio) + "}";
        sendJson(exchange, 200, json);
    }

    private void handleCola(HttpExchange exchange) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,\"clientes\":[");
        service.recorrerCola(new Visitante<Cliente>() {
            private boolean primero = true;

            @Override
            public void aceptar(Cliente cliente) {
                if (!primero) {
                    sb.append(',');
                }
                sb.append(toJson(cliente));
                primero = false;
            }
        });
        sb.append("]}");
        sendJson(exchange, 200, sb.toString());
    }

    private void handleHistorial(HttpExchange exchange) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,\"clientes\":[");
        service.recorrerHistorial(new Visitante<Cliente>() {
            private boolean primero = true;

            @Override
            public void aceptar(Cliente cliente) {
                if (!primero) {
                    sb.append(',');
                }
                sb.append(toJson(cliente));
                primero = false;
            }
        });
        sb.append("]}");
        sendJson(exchange, 200, sb.toString());
    }

    private void handleActual(HttpExchange exchange) throws IOException {
        Cliente cliente = service.getUltimoAtendido();
        String json = cliente == null
            ? "{\"ok\":true,\"cliente\":null}"
            : "{\"ok\":true,\"cliente\":" + toJson(cliente) + "}";
        sendJson(exchange, 200, json);
    }

    private void handleCancelar(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"ok\":false,\"mensaje\":\"Metodo no permitido\"}");
            return;
        }
        Map<String, String> data = HttpUtils.parseForm(HttpUtils.readBody(exchange.getRequestBody()));
        try {
            String documento = data.get("documento");
            TipoDocumento tipoDocumento = TipoDocumento.valueOf(data.getOrDefault("tipoDocumento", ""));
            boolean ok = service.cancelar(tipoDocumento, documento);
            String json = "{\"ok\":" + ok + "}";
            sendJson(exchange, 200, json);
        } catch (Exception ex) {
            String json = "{\"ok\":false,\"mensaje\":\"" + HttpUtils.escapeJson(ex.getMessage()) + "\"}";
            sendJson(exchange, 400, json);
        }
    }

    private String toJson(Cliente cliente) {
        if (cliente == null) {
            return "null";
        }
        return "{\"turno\":" + cliente.getTurno()
            + ",\"nombre\":\"" + HttpUtils.escapeJson(cliente.getNombre()) + "\""
            + ",\"tipo\":\"" + cliente.getTipo().name() + "\""
            + ",\"tipoDocumento\":\"" + cliente.getTipoDocumento().name() + "\""
            + ",\"documento\":\"" + HttpUtils.escapeJson(cliente.getDocumento()) + "\"" + "}";
    }

    private void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=UTF-8");
        headers.set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static class StaticHandler implements HttpHandler {
        private final Path webRoot = Path.of("web");

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            Path file = webRoot.resolve(path.substring(1)).normalize();
            if (!file.startsWith(webRoot) || !Files.exists(file)) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            String contentType = getContentType(file);
            byte[] bytes = Files.readAllBytes(file);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String getContentType(Path file) {
            String name = file.getFileName().toString();
            if (name.endsWith(".css")) {
                return "text/css; charset=UTF-8";
             slowed down;
            }
            if (name.endsWith(".js")) {
                return "application/javascript; charset=UTF-8";
            }
            return "text/html; charset=UTF-8";
        }
    }
}
