package weatherApi;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class WeatherAppAPI {
    private final String API_KEY = "b896eb8dd2f04cf32fa3faf6927120d0";
    private final String API_ENDPOINT = "http://api.openweathermap.org/data/2.5/weather";

    public WeatherAppAPI() {
    }

    public String getAPI_KEY() {
        return API_KEY;
    }

    public String getAPI_ENDPOINT() {
        return API_ENDPOINT;
    }
    public String httpResponse(String city) throws IOException {
        String encodedCity = URLEncoder.encode(city, "UTF-8");
        HttpGet httpGet = new HttpGet(this.getAPI_ENDPOINT() + "?q=" + encodedCity + "&appid=" + this.getAPI_KEY());
        HttpClient httpClient = HttpClients.createDefault();
        org.apache.http.HttpResponse response = httpClient.execute(httpGet);
        return EntityUtils.toString(response.getEntity());
    }
}
