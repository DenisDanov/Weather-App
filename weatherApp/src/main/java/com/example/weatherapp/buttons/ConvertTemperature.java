package com.example.weatherapp.buttons;

import com.example.weatherapp.labels.BubbleLabels;
import javafx.scene.control.Button;
import parsingWeatherData.WeatherData;

public class ConvertTemperature extends Button {

    private final BubbleLabels temperatureLabel;
    private final BubbleLabels temperatureFeelsLikeLabel;
    private WeatherData weatherData;

    public ConvertTemperature(BubbleLabels temperatureLabel, BubbleLabels temperatureFeelsLikeLabel,
                              WeatherData weatherData) {

        this.temperatureLabel = temperatureLabel;
        this.temperatureFeelsLikeLabel = temperatureFeelsLikeLabel;
        this.setForecastData(weatherData);

        configureButton();
    }

    public void setForecastData(WeatherData weatherData) {
        this.weatherData = weatherData;
    }

    private void configureButton() {
        setOnAction(actionEvent -> convertTemperature());
    }

    private void convertTemperature() {
        if (temperatureLabel.getText().contains("°C") && temperatureFeelsLikeLabel.getText().contains("°C")) {
            temperatureLabel.setText(String.format("Temperature: %.0f°F \uD83C\uDF21",
                    getTempInFahrenheit(weatherData.getCurrent().getTemp_c())));
            temperatureFeelsLikeLabel.setText(String.format("Feels like: %.0f°F \uD83C\uDF21",
                    getTempInFahrenheit(weatherData.getCurrent().getFeelsLikeC())));
        } else if (temperatureLabel.getText().contains("°F") && temperatureFeelsLikeLabel.getText().contains("°F")) {
            temperatureLabel.setText(String.format("Temperature: %.0fK \uD83C\uDF21",
                    getTempInKelvin(weatherData.getCurrent().getTemp_c())));
            temperatureFeelsLikeLabel.setText(String.format("Feels like: %.0fK \uD83C\uDF21",
                    getTempInKelvin(weatherData.getCurrent().getFeelsLikeC())));
        } else {
            temperatureLabel.setText(String.format("Temperature: %.0f°C \uD83C\uDF21",
                    getTempInCelsius(weatherData.getCurrent().getTemp_c())));
            temperatureFeelsLikeLabel.setText(String.format("Feels like: %.0f°C \uD83C\uDF21",
                    getTempInCelsius(weatherData.getCurrent().getFeelsLikeC())));
        }
    }

    private double getTempInCelsius(double temp) {
        return temp;
    }

    private double getTempInFahrenheit(double temp) {
        return (temp * (9 / 5) + 32);
    }
    private double getTempInKelvin(double temp) {
        return (temp - 32) * 5/9 + 273.15;
    }
}

