package com.example.weatherapp.buttons;

import com.example.weatherapp.Main;
import com.example.weatherapp.labels.BubbleLabels;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.json.JSONObject;
import parsingWeatherData.Current;
import parsingWeatherData.WeatherData;
import weatherApi.ForecastAPI;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public class ShowMoreWeatherData extends Button {

    private final BubbleLabels humidityLabel;
    private final BubbleLabels windSpeedLabel;
    private final BubbleLabels uvLabel;
    private final Button getDailyForecast;
    private final Label dateForecast;
    private final Label maxTempForecast;
    private final Label minTempForecast;
    private final Label avgTempForecast;
    private final Label maxWindForecast;
    private final Label avgHumidityForecast;
    private final Label chanceOfRainingForecast;
    private final Label chanceOfSnowForecast;
    private final Label weatherDescriptionForecast;
    private final Label sunrise;
    private final Label sunset;
    private final Button showWeeklyForecastButton;
    private WeatherData weatherData;
    private final ConvertWindSpeed convertWindSpeed;
    private String city;
    private final ConcurrentHashMap<String, String> responseBodiesDailySecondAPI;
    private final ForecastAPI forecastAPI;

    public ShowMoreWeatherData(BubbleLabels humidityLabel,
                               BubbleLabels windSpeedLabel,
                               BubbleLabels uvLabel,
                               Button getDailyForecast,
                               Label dateForecast,
                               Label maxTempForecast,
                               Label minTempForecast,
                               Label avgTempForecast,
                               Label maxWindForecast,
                               Label avgHumidityForecast,
                               Label chanceOfRainingForecast,
                               Label chanceOfSnowForecast,
                               Label weatherDescriptionForecast,
                               Label sunrise,
                               Label sunset,
                               Button showWeeklyForecastButton,
                               WeatherData weatherData,
                               ConvertWindSpeed convertWindSpeed,
                               String city,
                               ConcurrentHashMap<String, String> responseBodiesDailySecondAPI,
                               ForecastAPI forecastAPI) {

        this.humidityLabel = humidityLabel;
        this.windSpeedLabel = windSpeedLabel;
        this.uvLabel = uvLabel;
        this.getDailyForecast = getDailyForecast;
        this.dateForecast = dateForecast;
        this.maxTempForecast = maxTempForecast;
        this.minTempForecast = minTempForecast;
        this.avgTempForecast = avgTempForecast;
        this.maxWindForecast = maxWindForecast;
        this.avgHumidityForecast = avgHumidityForecast;
        this.chanceOfRainingForecast = chanceOfRainingForecast;
        this.chanceOfSnowForecast = chanceOfSnowForecast;
        this.weatherDescriptionForecast = weatherDescriptionForecast;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.showWeeklyForecastButton = showWeeklyForecastButton;
        this.setWeatherData(weatherData);
        this.convertWindSpeed = convertWindSpeed;
        this.setCity(city);
        this.responseBodiesDailySecondAPI = responseBodiesDailySecondAPI;
        this.forecastAPI = forecastAPI;

        configureButton();
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setWeatherData(WeatherData weatherData) {
        this.weatherData = weatherData;
    }

    private void configureButton() {
        setOnAction(actionEvent -> {
            try {
                showMoreButtonAction();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void showMoreButtonAction() throws IOException {

        Current mainInfo = weatherData.getMain();

        if (humidityLabel.getText().equals("") && !humidityLabel.isVisible()) {
            showLabels(mainInfo);
        } else {
            hideLabels();
        }

    }

    public void showLabels(Current mainInfo) {
        humidityLabel.setText(String.format("Humidity: %d %%", mainInfo.getHumidity()));
        uvLabel.setText("UV Index: " + Main.getUvOutputFormat(getUV(city)));
        windSpeedLabel.setText(String.format("Wind speed: %.0f km/h", (weatherData.getMain().getSpeed() * 3.6)));
        convertWindSpeed.setVisible(true);
        getDailyForecast.setVisible(true);
        humidityLabel.setVisible(true);
        windSpeedLabel.setVisible(true);
        uvLabel.setVisible(true);
    }

    public double getUV(String city) {
        String responseBody;
        if (!responseBodiesDailySecondAPI.containsKey(city)) {
            try {
                responseBody = forecastAPI.httpResponseDailyForecast(city);
            } catch (IOException e) {
                throw new RuntimeException();
            }
            responseBodiesDailySecondAPI.put(city, Objects.requireNonNull(responseBody));
        } else {
            responseBody = responseBodiesDailySecondAPI.get(city);
        }

        JSONObject jsonObject = new JSONObject(responseBody);
        JSONObject currentObject = jsonObject.getJSONObject("current");

        return currentObject.getDouble("uv");
    }

    public void hideLabels() {
        humidityLabel.setText("");
        humidityLabel.setVisible(false);
        windSpeedLabel.setVisible(false);
        uvLabel.setVisible(false);
        convertWindSpeed.setVisible(false);
        getDailyForecast.setVisible(false);
        showWeeklyForecastButton.setVisible(false);
        if (dateForecast.isVisible()) {
            dateForecast.setText("");
            dateForecast.setVisible(false);
            weatherDescriptionForecast.setVisible(false);
            maxTempForecast.setVisible(false);
            minTempForecast.setVisible(false);
            avgTempForecast.setVisible(false);
            maxWindForecast.setVisible(false);
            avgHumidityForecast.setVisible(false);
            chanceOfRainingForecast.setVisible(false);
            chanceOfSnowForecast.setVisible(false);
            sunrise.setVisible(false);
            sunset.setVisible(false);
        }
    }
}
