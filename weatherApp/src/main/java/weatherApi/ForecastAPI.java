package weatherApi;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;

public class ForecastAPI {
    private static final String API_KEY = "5fdd02c445514782a5b85549232808";
    private static final String API_ENDPOINT = "http://api.weatherapi.com/v1";
    public static String httpResponse(String city) throws IOException {
        String encodedCity = URLEncoder.encode(city, "UTF-8");
        HttpGet httpGet = new HttpGet("http://api.weatherapi.com/v1/current.json?key=5fdd02c445514782a5b85549232808&q=" + encodedCity + "&aqi=no");
        HttpClient httpClient = HttpClients.createDefault();
        org.apache.http.HttpResponse response = httpClient.execute(httpGet);
        return EntityUtils.toString(response.getEntity());
    }
    public static String httpResponseForecast(String city) throws IOException {
        String encodedCity = URLEncoder.encode(city, "UTF-8");
        HttpGet httpGet = new HttpGet("http://api.weatherapi.com/v1/forecast.json?key=5fdd02c445514782a5b85549232808&q=" + encodedCity + "&days=7" + "&aqi=no&alerts=no");
        HttpClient httpClient = HttpClients.createDefault();
        org.apache.http.HttpResponse response = httpClient.execute(httpGet);
        return EntityUtils.toString(response.getEntity());
    }
}
