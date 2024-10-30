import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ClienteHTTP {
    public String obtenerDatos(String baseMoneda) {
        try {
            // URL de la API
            String url = "https://v6.exchangerate-api.com/v6/a163be0844e904505b20307b/latest/" + baseMoneda;

            // Abrir conexión HTTP
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // Configuración del request
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();

            // Verificar que la respuesta sea 200 OK
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                throw new RuntimeException("Error en la solicitud HTTP: " + responseCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error en la solicitud HTTP: " + e.getMessage());
        }
    }
}
