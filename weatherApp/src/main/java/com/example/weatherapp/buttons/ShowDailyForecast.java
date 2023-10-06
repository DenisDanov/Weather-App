package com.example.weatherapp.buttons;

import com.example.weatherapp.Main;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import parsingWeatherData.WeatherData;

import java.util.Objects;

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
        // daily forecast logic
        if (dateForecast.getText().equals("") && !dateForecast.isVisible()) {
            updateLabels(Main.weatherData);
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

    public void updateLabels(WeatherData finalWeatherData) {

        dateForecast.setText(String.format("Date: %s", Objects.requireNonNull(finalWeatherData).getForecast().getForecastday().get(0).getDate()));
        weatherDescriptionForecast.setText("Weather description for the day: " + finalWeatherData.getForecast().getForecastday().get(0).getDay().getCondition().getText());
        maxTempForecast.setText(String.format("Max temperature for the day: %.0f°C", finalWeatherData.getForecast().getForecastday().get(0).getDay().getMaxTempC()));
        minTempForecast.setText(String.format("Min temperature for the day: %.0f°C", finalWeatherData.getForecast().getForecastday().get(0).getDay().getMinTempC()));
        avgTempForecast.setText(String.format("UV Index for the day: %s", getUvOutputFormat(finalWeatherData.getForecast().getForecastday().get(0).getDay().getUv())));
        maxWindForecast.setText(String.format("Max wind speed for the day: %.0f km/h", finalWeatherData.getForecast().getForecastday().get(0).getDay().getMaxWindKph()));
        avgHumidityForecast.setText(String.format("Average humidity for the day: %.0f %%", finalWeatherData.getForecast().getForecastday().get(0).getDay().getAvgHumidity()));
        chanceOfRainingForecast.setText(String.format("Chance of raining: %d %%", finalWeatherData.getForecast().getForecastday().get(0).getDay().getDailyChanceOfRain()));
        chanceOfSnowForecast.setText(String.format("Chance of snowing: %d %%", finalWeatherData.getForecast().getForecastday().get(0).getDay().getDailyChanceOfSnow()));
        sunrise.setText("Sunrise: " + finalWeatherData.getForecast().getForecastday().get(0).getAstro().getSunrise());
        sunset.setText("Sunset: " + finalWeatherData.getForecast().getForecastday().get(0).getAstro().getSunset());
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

