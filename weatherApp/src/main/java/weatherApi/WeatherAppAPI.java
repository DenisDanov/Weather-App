package weatherApi;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class WeatherAppAPI {

    private static final String API_KEY = "b896eb8dd2f04cf32fa3faf6927120d0";

    private static final String API_ENDPOINT = "http://api.openweathermap.org/data/2.5/weather";

    public static String httpResponse(String city) throws IOException {
        String encodedCity = URLEncoder.encode(city, "UTF-8");
        String urlString = API_ENDPOINT + "?q=" + encodedCity + "&appid=" + API_KEY;

        // Create a URL object
        URL url = new URL(urlString);

        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the HTTP request method (GET in this case)
        connection.setRequestMethod("GET");

        // Set the request headers if needed
        // connection.setRequestProperty("HeaderName", "HeaderValue");

        // Get the HTTP response code
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // If the response code is 200 (OK), read the response data
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Close the connection
            connection.disconnect();

            return response.toString();
        } else {
            // Handle non-OK response codes here, e.g., log an error message
            System.err.println("HTTP request failed with status code: " + responseCode);
            return null; // or throw an exception
        }
    }
}
