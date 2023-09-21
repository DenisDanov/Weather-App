package com.example.weatherapp;

import parsingWeatherData.ForecastData;
import parsingWeatherData.WeatherData;

public class WeatherDataAndForecast {
    private final String responseBody;
    private final WeatherData weatherData;
    private final ForecastData forecastData;
    private final String weatherConditionAndIcon;
    private final String localTime;
    public WeatherDataAndForecast(String responseBody, WeatherData weatherData, ForecastData forecastData,
                                  String weatherConditionAndIcon,
                                  String localTime) {
        this.responseBody = responseBody;
        this.weatherData = weatherData;
        this.forecastData = forecastData;
        this.weatherConditionAndIcon = weatherConditionAndIcon;
        this.localTime = localTime;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public String getWeatherConditionAndIcon() {
        return weatherConditionAndIcon;
    }

    public WeatherData getWeatherData() {
        return weatherData;
    }

    public ForecastData getForecastData() {
        return forecastData;
    }

    public String getLocalTime() {
        return localTime;
    }
}
