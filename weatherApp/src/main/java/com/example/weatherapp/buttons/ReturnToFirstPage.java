package com.example.weatherapp.buttons;

import com.example.weatherapp.labels.BubbleLabels;
import com.example.weatherapp.Main;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ReturnToFirstPage extends Button {

    private Stage stage;
    private Scene firstPageScene;
    private Label invalidInput;
    private VBox firstPageVbox;
    private Button fetchButton;
    private TextField cityStartUpTextField;
    private TextField inputTextField;
    private BubbleLabels temperatureLabel;

    public ReturnToFirstPage(Stage stage,
            Scene firstPageScene,
            Label invalidInput,
            VBox firstPageVbox,
            Button fetchButton,
            TextField cityStartUpTextField,
            TextField inputTextField,
            BubbleLabels temperatureLabel) {

        this.setStage(stage);
        this.setFirstPageScene(firstPageScene);
        this.setInvalidInput(invalidInput);
        this.setFirstPageVbox(firstPageVbox);
        this.setFetchButton(fetchButton);
        this.setCityStartUpTextField(cityStartUpTextField);
        this.setInputTextField(inputTextField);
        this.setTemperatureLabel(temperatureLabel);

        configureButton();
    }

    private void returnBackToFirstPage() {
        stage.setScene(firstPageScene);
        Main.passedFirstPage = "not passed";
        invalidInput.setText("");

        if (!firstPageVbox.getChildren().contains(fetchButton)) {
            firstPageVbox.getChildren().add(2, fetchButton);
            cityStartUpTextField.setText(inputTextField.getText());
            cityStartUpTextField.setStyle(temperatureLabel.getStyle());
            inputTextField.setText("");
            Platform.runLater(() -> cityStartUpTextField.positionCaret(cityStartUpTextField.getText().length()));
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setFirstPageScene(Scene firstPageScene) {
        this.firstPageScene = firstPageScene;
    }

    public void setInvalidInput(Label invalidInput) {
        this.invalidInput = invalidInput;
    }

    public void setFirstPageVbox(VBox firstPageVbox) {
        this.firstPageVbox = firstPageVbox;
    }

    public void setFetchButton(Button fetchButton) {
        this.fetchButton = fetchButton;
    }

    public void setCityStartUpTextField(TextField cityStartUpTextField) {
        this.cityStartUpTextField = cityStartUpTextField;
    }

    public void setInputTextField(TextField inputTextField) {
        this.inputTextField = inputTextField;
    }

    public void setTemperatureLabel(BubbleLabels temperatureLabel) {
        this.temperatureLabel = temperatureLabel;
    }

    private void configureButton() {
        setOnAction(actionEvent -> returnBackToFirstPage());
    }
}
