package com.example.weatherapp.buttons;

import com.example.weatherapp.labels.BubbleLabels;
import javafx.scene.control.Button;
import parsingWeatherData.MainParsedData;
import parsingWeatherData.WeatherData;

public class ConvertTemperature extends Button {

    private final BubbleLabels temperatureLabel;
    private final BubbleLabels temperatureFeelsLikeLabel;
    private WeatherData weatherData;

    public ConvertTemperature(BubbleLabels temperatureLabel, BubbleLabels temperatureFeelsLikeLabel) {

        this.temperatureLabel = temperatureLabel;
        this.temperatureFeelsLikeLabel = temperatureFeelsLikeLabel;
        this.setWeatherData(weatherData);

        configureButton();
    }

    public void setWeatherData(WeatherData weatherData) {
        this.weatherData = weatherData;
    }

    private void configureButton() {
        setOnAction(actionEvent -> convertTemperature());
    }

    private void convertTemperature() {
        MainParsedData mainInfo = weatherData.getMain();
        if (temperatureLabel.getText().contains("°C") && temperatureFeelsLikeLabel.getText().contains("°C")) {
            temperatureLabel.setText(String.format("Temperature: %.0f°F \uD83C\uDF21",
                    getTempInFahrenheit(mainInfo.getTemp())));
            temperatureFeelsLikeLabel.setText(String.format("Feels like: %.0f°F \uD83C\uDF21",
                    getTempInFahrenheit(mainInfo.getFeels_like())));
        } else if (temperatureLabel.getText().contains("°F") && temperatureFeelsLikeLabel.getText().contains("°F")) {
            temperatureLabel.setText(String.format("Temperature: %.0f K \uD83C\uDF21",
                    (mainInfo.getTemp())));
            temperatureFeelsLikeLabel.setText(String.format("Feels like: %.0f K \uD83C\uDF21",
                    (mainInfo.getFeels_like())));
        } else {
            temperatureLabel.setText(String.format("Temperature: %.0f°C \uD83C\uDF21",
                    getTempInCelsius(mainInfo.getTemp())));
            temperatureFeelsLikeLabel.setText(String.format("Feels like: %.0f°C \uD83C\uDF21",
                    getTempInCelsius(mainInfo.getFeels_like())));
        }
    }

    private double getTempInCelsius(double temp) {
        return temp - 273.15;
    }

    private double getTempInFahrenheit(double temp) {
        return (temp - 273.15) * 9 / 5 + 32;
    }
}

