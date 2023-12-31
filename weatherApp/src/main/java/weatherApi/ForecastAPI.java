package weatherApi;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;

public class ForecastAPI {

    private static final String API_KEY = "adccb04745694803aed162619231211";

    private static final String API_ENDPOINT = "https://api.weatherapi.com/v1";

    private final CloseableHttpClient HTTP_CLIENT;

    public ForecastAPI() {
        HTTP_CLIENT = HttpClients.createDefault();
    }

    public String httpResponseDailyForecast(String city) throws IOException {
        String encodedCity = URLEncoder.encode(city, "UTF-8");
        HttpGet httpGet = new HttpGet("https://api.weatherapi.com/v1" + "/forecast.json?key=" + API_KEY + "&q=" +
                encodedCity + "&days=1" + "&aqi=no&alerts=no");

        try (CloseableHttpResponse response = HTTP_CLIENT.execute(httpGet)) {

            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);
            System.out.println(responseString);
            if (!responseString.contains("error")) {
                return responseString;
            } else {
                return null;
            }
        }
    }

    public String httpResponseWeeklyForecast(String city) throws IOException {
        String encodedCity = URLEncoder.encode(city, "UTF-8");
        HttpGet httpGet = new HttpGet(API_ENDPOINT + "/forecast.json?key=" + API_KEY + "&q=" +
                encodedCity + "&days=7" + "&aqi=no&alerts=no");

        try (CloseableHttpResponse response = HTTP_CLIENT.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);
            if (!responseString.contains("error")) {
                return responseString;
            } else {
                return null;
            }
        }
    }
}
