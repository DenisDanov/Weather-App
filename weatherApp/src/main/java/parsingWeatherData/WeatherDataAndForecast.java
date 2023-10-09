package parsingWeatherData;

import parsingWeatherData.WeatherData;

public class WeatherDataAndForecast {

    private final WeatherData weatherData;
    private final String weatherConditionAndIcon;
    private final String localTime;

    public WeatherDataAndForecast(WeatherData weatherData,
                                  String weatherConditionAndIcon,
                                  String localTime) {
        this.weatherData = weatherData;
        this.weatherConditionAndIcon = weatherConditionAndIcon;
        this.localTime = localTime;
    }

    public String getWeatherConditionAndIcon() {
        return weatherConditionAndIcon;
    }

    public WeatherData getForecastData() {
        return weatherData;
    }

    public String getLocalTime() {
        return localTime;
    }
}
