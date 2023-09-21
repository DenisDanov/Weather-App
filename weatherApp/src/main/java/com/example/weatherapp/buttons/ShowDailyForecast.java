package com.example.weatherapp.buttons;

import com.example.weatherapp.Main;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import parsingWeatherData.ForecastData;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.weatherapp.Main.getUvOutputFormat;

public class ShowDailyForecast extends Button {

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

    public ShowDailyForecast(Label dateForecast,
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
                             Button showWeeklyForecastButton) {

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

        configureButton();
    }

    private void configureButton() {
        setOnAction(actionEvent -> showDailyForecastAction());
    }

    public void showDailyForecastAction() {
        // Get daily forecast logic
        if (dateForecast.getText().equals("") && !dateForecast.isVisible()) {
            updateLabels(Main.forecastData);
        } else {
            hideLabels();
        }
    }

    public void hideLabels() {
        if (showWeeklyForecastButton.isVisible()) {
            showWeeklyForecastButton.setVisible(false);
        }
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

    public void updateLabels(ForecastData finalForecastData) {

        dateForecast.setText(String.format("Date: %s", Objects.requireNonNull(finalForecastData).getDate()));
        weatherDescriptionForecast.setText("Weather description for the day: " + finalForecastData.getWeatherDescription());
        maxTempForecast.setText(String.format("Max temperature for the day: %.0f°C", finalForecastData.getMaxTemp()));
        minTempForecast.setText(String.format("Min temperature for the day: %.0f°C", finalForecastData.getMinTemp()));
        avgTempForecast.setText(String.format("UV Index for the day: %s", getUvOutputFormat(finalForecastData.getUvIndex())));
        maxWindForecast.setText(String.format("Max wind speed for the day: %.0f km/h", finalForecastData.getMaxWind()));
        avgHumidityForecast.setText(String.format("Average humidity for the day: %.0f %%", finalForecastData.getAvgHumidity()));
        chanceOfRainingForecast.setText(String.format("Chance of raining: %.0f %%", finalForecastData.getPercentChanceOfRain()));
        chanceOfSnowForecast.setText(String.format("Chance of snowing: %.0f %%", finalForecastData.getPercentChanceOfSnow()));
        sunrise.setText("Sunrise: " + finalForecastData.getSunRise());
        sunset.setText("Sunset: " + finalForecastData.getSunSet());
        if (!dateForecast.isVisible()) {
            dateForecast.setVisible(true);
            weatherDescriptionForecast.setVisible(true);
            maxTempForecast.setVisible(true);
            minTempForecast.setVisible(true);
            avgTempForecast.setVisible(true);
            maxWindForecast.setVisible(true);
            avgHumidityForecast.setVisible(true);
            chanceOfRainingForecast.setVisible(true);
            chanceOfSnowForecast.setVisible(true);
            sunrise.setVisible(true);
            sunset.setVisible(true);
            showWeeklyForecastButton.setVisible(true);
        }

    }
}

