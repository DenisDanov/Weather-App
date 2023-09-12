package parsingWeatherData;

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

