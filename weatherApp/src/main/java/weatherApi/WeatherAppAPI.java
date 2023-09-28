package weatherApi;

import org.apache.http.HttpEntity;
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

    private final CloseableHttpClient HTTP_CLIENT;

    public WeatherAppAPI() {
        HTTP_CLIENT = HttpClients.custom()
                .setMaxConnTotal(1) // Maximum total connections
                .setMaxConnPerRoute(1) // Maximum connections per route
                .build();
    }

    public String httpResponse(String city) throws IOException {
        String encodedCity = URLEncoder.encode(city, "UTF-8");
        String urlString = API_ENDPOINT + "?q=" + encodedCity + "&appid=" + API_KEY;
        HttpGet httpGet = new HttpGet(urlString);
        // Create a URL object
        try (CloseableHttpResponse response = HTTP_CLIENT.execute(httpGet)) {

            HttpEntity entity = response.getEntity();
            return  EntityUtils.toString(entity);
        }
    }
}
