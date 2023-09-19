package weatherApi;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;

public class WeatherAppAPI {

    private final String API_KEY = "b896eb8dd2f04cf32fa3faf6927120d0";

    private final String API_ENDPOINT = "http://api.openweathermap.org/data/2.5/weather";

    public String getAPI_KEY() {
        return API_KEY;
    }

    public String getAPI_ENDPOINT() {
        return API_ENDPOINT;
    }
    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.custom()
            .setMaxConnTotal(100) // Maximum total connections
            .setMaxConnPerRoute(20) // Maximum connections per route
            .build();

    public WeatherAppAPI() {
    }

    public String httpResponse(String city) throws IOException {
        String encodedCity = URLEncoder.encode(city, "UTF-8");
        HttpGet httpGet = new HttpGet(getAPI_ENDPOINT() + "?q=" + encodedCity + "&appid=" + getAPI_KEY());
        try (CloseableHttpResponse response = HTTP_CLIENT.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity);
        }
    }
}
