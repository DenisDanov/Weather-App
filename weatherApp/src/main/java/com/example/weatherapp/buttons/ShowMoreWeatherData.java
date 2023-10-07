package com.example.weatherapp.buttons;

import com.example.weatherapp.Main;
import com.example.weatherapp.labels.BubbleLabels;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import parsingWeatherData.WeatherData;
import java.io.IOException;

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
    private final ConvertWindSpeed convertWindSpeed;
    private WeatherData weatherData;

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
                               ConvertWindSpeed convertWindSpeed,
                               WeatherData weatherData) {

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
        this.setForecastData(weatherData);
        this.convertWindSpeed = convertWindSpeed;
        this.setForecastData(weatherData);

        configureButton();
    }

    public void setForecastData(WeatherData weatherData) {
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

        if (humidityLabel.getText().equals("") && !humidityLabel.isVisible()) {
            showLabels();
        } else {
            hideLabels();
        }

    }

    public void showLabels() {
        humidityLabel.setText(String.format("Humidity: %d %%", weatherData.getCurrent().getHumidity()));
        uvLabel.setText("UV Index: " + Main.getUvOutputFormat(weatherData.getCurrent().getUv()));
        windSpeedLabel.setText(String.format("Wind speed: %.0f km/h", (weatherData.getCurrent().getWindKph())));
        convertWindSpeed.setVisible(true);
        getDailyForecast.setVisible(true);
        humidityLabel.setVisible(true);
        windSpeedLabel.setVisible(true);
        uvLabel.setVisible(true);
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
