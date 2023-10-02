package com.example.weatherapp.buttons;

import com.example.weatherapp.labels.BubbleLabels;
import javafx.scene.control.Button;
import parsingWeatherData.WeatherData;

public class ConvertWindSpeed extends Button {

    private final BubbleLabels windSpeedLabel;
    private WeatherData weatherData;

    public ConvertWindSpeed(BubbleLabels windSpeedLabel) {
        this.windSpeedLabel = windSpeedLabel;
        this.setWeatherData(weatherData);

        configureButton();
    }

    public void setWeatherData(WeatherData weatherData) {
        this.weatherData = weatherData;
    }

    private void configureButton() {
        setOnAction(actionEvent -> convertWindSpeed());
    }

    private void convertWindSpeed() {
        // Convert wind speed logic
        if (windSpeedLabel.getText().contains("km/h")) {
            windSpeedLabel.setText(String.format("Wind speed: %.0f mph",
                getWindSpeedInMiles(weatherData.getMain().getSpeed())));
        } else if (windSpeedLabel.getText().contains("mph")) {
            windSpeedLabel.setText(String.format("Wind speed: %.0f km/h",
                getWindSpeedInKms(weatherData.getMain().getSpeed())));
        }
    }

    private double getWindSpeedInKms(double windSpeed) {
        return windSpeed * 3.6;
    }

    private double getWindSpeedInMiles(double windSpeed) {
        return windSpeed * 2.23694;
    }
}
