package weatherApi;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;

public class ForecastAPI {

    private static final String API_KEY = "d9b2b634fbcf4c9190680834231109";

    private static final String API_ENDPOINT = "https://api.weatherapi.com/v1";

    public static String httpResponseForecast(String city) throws IOException {
        String encodedCity = URLEncoder.encode(city, "UTF-8");
        HttpGet httpGet = new HttpGet(
                API_ENDPOINT + "/forecast.json?key=" + API_KEY + "&q=" +
                        encodedCity + "&days=7" + "&aqi=no&alerts=no");
        HttpClient httpClient = HttpClients.createDefault();
        org.apache.http.HttpResponse response = httpClient.execute(httpGet);
        return EntityUtils.toString(response.getEntity());
    }
}
