package com.example.weatherapp.buttons;

import com.example.weatherapp.BubbleLabels;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import parsingWeatherData.WeatherData;

public class ButtonDependencyUpdater {
    private final ConvertTemperature convertTemperature;
    private final ConvertWindSpeed convertWindSpeed;
    private final ReturnToFirstPage returnBackToFirstPage;
    public ButtonDependencyUpdater(
            ConvertTemperature convertTemperature,
            ConvertWindSpeed convertWindSpeed,
            ReturnToFirstPage returnBackToFirstPage) {
        this.convertTemperature = convertTemperature;
        this.convertWindSpeed = convertWindSpeed;
        this.returnBackToFirstPage = returnBackToFirstPage;
    }

    public void updateButtonDependencies(
            WeatherData weatherData,
            Stage stage,
            Scene firstPageScene,
            Label invalidInput,
            VBox firstPageVbox,
            Button fetchButton,
            TextField cityStartUpTextField,
            TextField inputTextField,
            BubbleLabels temperatureLabel
    ) {
        convertTemperature.setWeatherData(weatherData);
        convertWindSpeed.setWeatherData(weatherData);

        returnBackToFirstPage.setStage(stage);
        returnBackToFirstPage.setFirstPageScene(firstPageScene);
        returnBackToFirstPage.setInvalidInput(invalidInput);
        returnBackToFirstPage.setFirstPageVbox(firstPageVbox);
        returnBackToFirstPage.setFetchButton(fetchButton);
        returnBackToFirstPage.setCityStartUpTextField(cityStartUpTextField);
        returnBackToFirstPage.setInputTextField(inputTextField);
        returnBackToFirstPage.setTemperatureLabel(temperatureLabel);
    }
}
