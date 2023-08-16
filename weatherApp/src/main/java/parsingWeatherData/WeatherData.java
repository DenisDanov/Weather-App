package parsingWeatherData;

import com.example.weatherapp.Main;
import com.google.gson.Gson;

public class WeatherData {
    private MainParsedData main;
    private WeatherInfo[] weather;
    private WindInfo wind;
    public MainParsedData getMain() {
        return main;
    }

    public WeatherInfo[] getWeather() {
        return weather;
    }

    public WindInfo getWind() {
        return wind;
    }
}

