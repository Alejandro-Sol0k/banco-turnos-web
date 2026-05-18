import java.io.IOException;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {
    public static Map<String, String> load(String ruta) {
        Map<String, String> valores = new HashMap<>();
        Path path = Path.of(ruta);
        if (!Files.exists(path)) {
            return valores;
        }
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String limpia = linea.trim();
                if (limpia.isEmpty() || limpia.startsWith("#")) {
                    continue;
                }
                int idx = limpia.indexOf('=');
                if (idx == -1) {
                    continue;
                }
                String clave = limpia.substring(0, idx).trim();
                String valor = limpia.substring(idx + 1).trim();
                valores.put(clave, valor);
            }
        } catch (IOException ex) {
            System.out.println("No se pudo leer .env: " + ex.getMessage());
        }
        return valores;
    }
}
