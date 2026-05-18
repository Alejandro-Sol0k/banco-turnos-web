import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {
    public static String readBody(InputStream body) throws IOException {
        return new String(body.readAllBytes(), StandardCharsets.UTF_8);
    }

    public static Map<String, String> parseForm(String body) {
        Map<String, String> valores = new HashMap<>();
        if (body == null || body.isEmpty()) {
            return valores;
        }
        int i = 0;
        int n = body.length();
        while (i < n) {
            int keyStart = i;
            while (i < n && body.charAt(i) != '=' && body.charAt(i) != '&') {
                i++;
            }
            int keyEnd = i;
            String clave = "";
            if (keyEnd > keyStart) {
                clave = URLDecoder.decode(body.substring(keyStart, keyEnd), StandardCharsets.UTF_8);
            }
            if (i < n && body.charAt(i) == '=') {
                i++;
            }
            int valStart = i;
            while (i < n && body.charAt(i) != '&') {
                i++;
            }
            int valEnd = i;
            String valor = "";
            if (valEnd > valStart) {
                valor = URLDecoder.decode(body.substring(valStart, valEnd), StandardCharsets.UTF_8);
            }
            if (!clave.isEmpty()) {
                valores.put(clave, valor);
            }
            if (i < n && body.charAt(i) == '&') {
                i++;
            }
        }
        return valores;
    }

    public static String escapeJson(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
