package com.example.weatherapp;

import com.example.weatherapp.buttons.*;
import com.example.weatherapp.dynamicBackground.DynamicBackgroundImpl;
import com.example.weatherapp.labels.BubbleLabels;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import parsingWeatherData.WeatherData;
import parsingWeatherData.WeatherDataAndForecast;
import weatherApi.ForecastAPI;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application {

    private static String city;
    private TextField inputTextField; // Main scene TextField for input
    private BubbleLabels localTimeLabel;
    private BubbleLabels temperatureLabel;
    private BubbleLabels descriptionLabel;
    private BubbleLabels temperatureFeelsLikeLabel;
    private BubbleLabels weatherDescriptionLabel;
    private BubbleLabels humidityLabel;
    private BubbleLabels windSpeedLabel;
    private BubbleLabels uvLabel;
    private ShowDailyForecast getDailyForecast;
    private Label dateForecast;
    private Label maxTempForecast;
    private Label minTempForecast;
    private Label avgTempForecast;
    private Label maxWindForecast;
    private Label avgHumidityForecast;
    private Label chanceOfRainingForecast;
    private Label chanceOfSnowForecast;
    private Label weatherDescriptionForecast;
    private Label sunrise;
    private Label sunset;
    private Label cityLabel;
    private ShowWeeklyForecast showWeeklyForecastButton;
    private ShowMoreWeatherData showMoreWeatherInfo;
    private ConvertTemperature convertTemperature;
    private ConvertWindSpeed convertWindSpeed;
    private Button fetchButton;
    private ReturnToFirstPage returnBackToFirstPage;
    private Scene mainScene;
    private Stage stage;
    private StackPane rootLayout;
    private VBox root;
    private VBox firstPageVbox;
    private final Label cityStartUpLabel = new Label("Enter City or Country:");
    private final Label invalidInput = new Label();
    private final TextField cityStartUpTextField = new TextField();
    private Scene firstPageScene;
    private GridPane buttonsPane;
    private final Pattern patternNums = Pattern.compile("[0-9]+");
    private static ConcurrentHashMap<String, String> responseBodiesDailySecondAPI;
    private String lastEnteredCity;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private GridPane gridPane;
    private ImageView iconView;
    private String responseBodyCheckForValidInput;
    public static String passedFirstPage;
    private DynamicBackgroundImpl dynamicBackground;
    private Image image;
    private final Map<String, Image> imageCache = new HashMap<>();
    public static WeatherData weatherData;
    private static ForecastAPI forecastAPI;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Main() {
        responseBodiesDailySecondAPI = new ConcurrentHashMap<>();
        this.responseBodyCheckForValidInput = "";
        passedFirstPage = "not passed!";
        this.lastEnteredCity = "";

        startScheduledTask();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        rootLayout = createRootLayout();
        mainScene = new Scene(rootLayout, 866, 700);
        stage = primaryStage;
        addStyleSheet(mainScene);
        configurePrimaryStage(primaryStage);
        primaryStage.show();
        configureFetchButton();
        setUpDynamicBackground();
        updateReturnButtonNodes();
    }

    private void updateReturnButtonNodes() {
        returnBackToFirstPage.setStage(stage);
        returnBackToFirstPage.setFirstPageScene(firstPageScene);
        returnBackToFirstPage.setInvalidInput(invalidInput);
        returnBackToFirstPage.setFirstPageVbox(firstPageVbox);
        returnBackToFirstPage.setFetchButton(fetchButton);
        returnBackToFirstPage.setCityStartUpTextField(cityStartUpTextField);
        returnBackToFirstPage.setInputTextField(inputTextField);
        returnBackToFirstPage.setTemperatureLabel(temperatureLabel);
        showWeeklyForecastButton.setRoot(root);
        showWeeklyForecastButton.setMainScene(mainScene);
        showWeeklyForecastButton.setStage(stage);
        showWeeklyForecastButton.setShowMoreWeatherInfo(showMoreWeatherInfo);
        showWeeklyForecastButton.setGetDailyForecast(getDailyForecast);
        dynamicBackground.setMainScene(mainScene);
        dynamicBackground.setStage(stage);
    }

    private void setUpDynamicBackground() {
        dynamicBackground = new DynamicBackgroundImpl(
                rootLayout,
                root,
                stage,
                mainScene,
                weatherData
        );
        dynamicBackground.addVideosPaths();
    }

    private void configurePrimaryStage(Stage primaryStage) {
        firstPageVbox = new VBox(5);
        firstPageVbox.setAlignment(Pos.CENTER);
        firstPageVbox.setPadding(new Insets(250));

        firstPageVbox.getChildren().addAll(
                cityStartUpLabel,
                cityStartUpTextField,
                fetchButton,
                invalidInput
        );
        firstPageScene = new Scene(firstPageVbox, 866, 700);
        firstPageScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/firstPage.css")).toExternalForm());
        primaryStage.setTitle("Weather App");
        primaryStage.setScene(firstPageScene);
    }

    private void addStyleSheet(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/mainPage.css")).toExternalForm());
        temperatureLabel.getStyleClass().add("emoji-label");
        temperatureFeelsLikeLabel.getStyleClass().add("emoji-label");
        Text labelText = new Text("Enter City or Country:");
        Font boldFont = Font.font("Arial", FontWeight.BOLD, 14);
        descriptionLabel.setMinHeight(30);
        labelText.setFont(boldFont);
        labelText.setFill(Color.WHITE);
        cityLabel.setGraphic(labelText);
    }

    private StackPane createRootLayout() {
        fetchButton = new Button("Show current weather");
        localTimeLabel = new BubbleLabels();
        temperatureLabel = new BubbleLabels();
        descriptionLabel = new BubbleLabels();
        temperatureFeelsLikeLabel = new BubbleLabels();
        weatherDescriptionLabel = new BubbleLabels();
        humidityLabel = new BubbleLabels();
        windSpeedLabel = new BubbleLabels();
        uvLabel = new BubbleLabels();
        dateForecast = new BubbleLabels();
        maxTempForecast = new BubbleLabels();
        minTempForecast = new BubbleLabels();
        avgTempForecast = new BubbleLabels();
        maxWindForecast = new BubbleLabels();
        avgHumidityForecast = new BubbleLabels();
        chanceOfRainingForecast = new BubbleLabels();
        chanceOfSnowForecast = new BubbleLabels();
        weatherDescriptionForecast = new BubbleLabels();
        sunrise = new BubbleLabels();
        sunset = new BubbleLabels();
        cityLabel = new Label();
        this.inputTextField = new TextField();
        rootLayout = new StackPane();
        root = new VBox();

        root.setSpacing(1.5);
        setRightMargin(inputTextField);

        iconView = new ImageView();
        gridPane = new GridPane();
        gridPane.add(weatherDescriptionLabel, 0, 0);
        gridPane.add(iconView, 1, 0);

        descriptionLabel.setGraphic(gridPane);
        descriptionLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        buttonsPane = new GridPane();
        buttonsPane.add(fetchButton, 0, 0);
        returnBackToFirstPage = new ReturnToFirstPage(
                stage,
                firstPageScene,
                invalidInput,
                firstPageVbox,
                fetchButton,
                cityStartUpTextField,
                inputTextField,
                temperatureLabel
        );
        returnBackToFirstPage.setText("Return to the first page");
        buttonsPane.add(returnBackToFirstPage, 1, 0);
        buttonsPane.setHgap(5);

        Objects.requireNonNull(root).getChildren().addAll(
                cityLabel,
                inputTextField,
                buttonsPane,
                localTimeLabel,
                temperatureLabel,
                temperatureFeelsLikeLabel,
                descriptionLabel
        );

        convertTemperature = new ConvertTemperature(temperatureLabel, temperatureFeelsLikeLabel, weatherData);
        convertTemperature.setText("Convert temperature");
        root.getChildren().add(6, convertTemperature);

        convertWindSpeed = new ConvertWindSpeed(windSpeedLabel, weatherData);
        convertWindSpeed.setText("Convert wind speed");

        showWeeklyForecastButton = new ShowWeeklyForecast(
                root,
                cityLabel,
                city,
                humidityLabel,
                windSpeedLabel,
                uvLabel,
                getDailyForecast,
                dateForecast,
                maxTempForecast,
                minTempForecast,
                avgTempForecast,
                maxWindForecast,
                avgHumidityForecast,
                chanceOfRainingForecast,
                chanceOfSnowForecast,
                weatherDescriptionForecast,
                sunrise,
                sunset,
                convertWindSpeed,
                inputTextField,
                localTimeLabel,
                temperatureLabel,
                descriptionLabel,
                temperatureFeelsLikeLabel,
                showMoreWeatherInfo,
                convertTemperature,
                fetchButton,
                mainScene,
                stage,
                forecastAPI
        );
        showWeeklyForecastButton.setText("Show weekly forecast");

        getDailyForecast = new ShowDailyForecast(
                dateForecast,
                maxTempForecast,
                minTempForecast,
                avgTempForecast,
                maxWindForecast,
                avgHumidityForecast,
                chanceOfRainingForecast,
                chanceOfSnowForecast,
                weatherDescriptionForecast,
                sunrise,
                sunset,
                showWeeklyForecastButton
        );
        getDailyForecast.setText("Show daily forecast");

        showMoreWeatherInfo = new ShowMoreWeatherData(
                humidityLabel,
                windSpeedLabel,
                uvLabel,
                getDailyForecast,
                dateForecast,
                maxTempForecast,
                minTempForecast,
                avgTempForecast,
                maxWindForecast,
                avgHumidityForecast,
                chanceOfRainingForecast,
                chanceOfSnowForecast,
                weatherDescriptionForecast,
                sunrise,
                sunset,
                showWeeklyForecastButton,
                convertWindSpeed,
                weatherData
        );

        showMoreWeatherInfo.setText("Show more weather info");

        root.getChildren().addAll(
                showMoreWeatherInfo,
                humidityLabel,
                uvLabel,
                windSpeedLabel,
                convertWindSpeed,
                getDailyForecast
        );

        root.getChildren().addAll(
                dateForecast,
                maxTempForecast,
                minTempForecast,
                avgTempForecast,
                maxWindForecast,
                avgHumidityForecast,
                chanceOfRainingForecast,
                chanceOfSnowForecast,
                weatherDescriptionForecast,
                sunrise,
                sunset,
                showWeeklyForecastButton
        );

        showWeeklyForecastButton.setVisible(false);
        convertTemperature.setVisible(false);
        showMoreWeatherInfo.setVisible(false);
        convertWindSpeed.setVisible(false);
        uvLabel.setVisible(false);
        humidityLabel.setVisible(false);
        windSpeedLabel.setVisible(false);
        Objects.requireNonNull(buttonsPane).setVisible(false);

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

        getDailyForecast.setVisible(false);
        return rootLayout;
    }

    public void startScheduledTask() {
        Runnable task = () -> {

            try {
                updateAPIData();
            } catch (ParseException | IOException e) {
                throw new RuntimeException(e);
            }
        };
        // Schedule the task to run every 5 minutes
        executorService.scheduleAtFixedRate(task, 5, 5, TimeUnit.MINUTES);
    }

    private void configureFetchButton() {
        fetchButton.setOnAction(event -> {
            // Fetch and display weather data
            if (stage.getScene() == firstPageScene) {
                checkForValidInput();
            } else {
                fetchAndDisplayWeatherData(inputTextField.getText());
            }
        });

        cityStartUpTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Call the same action as the button when Enter is pressed
                fetchButton.fire();
            }
        });

        inputTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Call the same action as the button when Enter is pressed
                fetchButton.fire();
            }
        });
    }

    private void setRightMargin(Region node) {
        Insets insets = new Insets(0, 590, 0, 0); // top, right, bottom, left
        VBox.setMargin(node, insets);
    }

    private WeatherDataAndForecast checkData(WeatherData weatherData,
                                             String weatherConditionAndIcon,
                                             String localTime) {
        if (weatherData != null) {
            return new WeatherDataAndForecast(weatherData, weatherConditionAndIcon, localTime);
        } else {
            return null;
        }
    }

    private void fetchAndDisplayWeatherData(String cityTextField) {
        city = cityTextField;

        if (!lastEnteredCity.equals(cityTextField)) {
            if (stage.getScene() == firstPageScene && !"Passed!".equals(passedFirstPage)) {
                checkForValidInput();
            } else {

                CompletableFuture<WeatherDataAndForecast> future = CompletableFuture.supplyAsync(() -> {

                    String weatherConditionAndIcon = getWeatherCondition();

                    Matcher matcher = patternNums.matcher(city);

                    if (!matcher.find() && weatherData != null) {

                        String localTime = formatDateToDayAndHour((weatherData.getLocation().getLocaltime()));
                        return checkData(weatherData, weatherConditionAndIcon, localTime);
                    } else {
                        return null;
                    }

                });
                future.thenAcceptAsync(validInput -> Platform.runLater(() -> {
                    if (validInput != null && validInput.getForecastData() != null
                            && !validInput.getWeatherConditionAndIcon().equals("")) {
                        WeatherData weatherData = validInput.getForecastData();
                        String weatherConditionAndIcon = validInput.getWeatherConditionAndIcon();
                        String localTime = validInput.getLocalTime();

                        updateButtonsData();
                        // Update UI with valid data
                        try {

                            dynamicBackground.switchVideoBackground(weatherConditionAndIcon.split("&")[1]);

                            String iconUrl = weatherConditionAndIcon.split("&")[0];
                            String completeIconUrl = "https:" + iconUrl;
                            if (image != null) {
                                if (!image.getUrl().equals(completeIconUrl)) {
                                    createImage(completeIconUrl);
                                }
                            } else {
                                createImage(completeIconUrl);
                            }

                            if (gridPane.getChildren().size() == 2) {
                                gridPane.getChildren().remove(1);
                                gridPane.add(iconView, 1, 0);
                            } else {
                                gridPane.add(iconView, 1, 0);
                            }

                            GridPane checkButtonsPane = (GridPane) root.getChildren().get(2);

                            if (!checkButtonsPane.getChildren().contains(fetchButton)) {
                                buttonsPane.add(fetchButton, 0, 0);
                            }

                            if (localTimeLabel.getTextFill().equals(Color.RED)) {
                                localTimeLabel.setTextFill(Color.WHITE);
                            }

                            if (inputTextField.getStyle().equals("-fx-text-fill: red;")) {
                                inputTextField.setStyle(temperatureLabel.getStyle());
                            }

                            buttonsPane.setVisible(true);
                            convertTemperature.setVisible(true);
                            showMoreWeatherInfo.setVisible(true);
                            temperatureLabel.setVisible(true);
                            descriptionLabel.setVisible(true);
                            temperatureFeelsLikeLabel.setVisible(true);

                            localTimeLabel.setText(String.format("Local time: %s", localTime));
                            temperatureLabel.setText(String.format("Temperature: %.0f°C \uD83C\uDF21",
                                    weatherData.getCurrent().getTemp_c()));
                            temperatureFeelsLikeLabel.setText(String.format("Feels like: %.0f°C \uD83C\uDF21",
                                    weatherData.getCurrent().getFeelsLikeC()));

                            weatherDescriptionLabel.setWrapText(true);
                            weatherDescriptionLabel.setText("Weather Description: " +
                                    weatherConditionAndIcon.split("&")[1]);

                            if (!humidityLabel.getText().equals("") && humidityLabel.isVisible()) {
                                showMoreWeatherInfo.showLabels();
                                if (!dateForecast.getText().equals("") && dateForecast.isVisible()) {
                                    getDailyForecast.updateLabels(weatherData);
                                } else {
                                    getDailyForecast.hideLabels();
                                }
                            }
                            if (!lastEnteredCity.equals(city)) {
                                lastEnteredCity = city;
                            }
                        } catch (Exception e) {
                            // case of invalid input or error
                            e.printStackTrace(System.out);
                            localTimeLabel.setText("An error occurred.");
                            hideAllNodes();
                        }
                    } else {
                        // case of invalid input or error
                        localTimeLabel.setText("Invalid place.");
                        hideAllNodes();
                    }

                    if (inputTextField.getText().equals("") && stage.getScene() == firstPageScene) {
                        inputTextField.setText(cityStartUpTextField.getText());
                    }

                    if (stage.getScene() != mainScene) {
                        stage.setScene(mainScene);
                    }

                    inputTextField.deselect();
                    Platform.runLater(() -> inputTextField.positionCaret(inputTextField.getText().length()));
                }));
            }
        } else {
            if (stage.getScene() == firstPageScene) {
                inputTextField.setText(cityStartUpTextField.getText());
                inputTextField.deselect();
                Platform.runLater(() -> inputTextField.positionCaret(inputTextField.getText().length()));
                if (!buttonsPane.getChildren().contains(fetchButton)) {
                    buttonsPane.add(fetchButton, 0, 0);
                }
                stage.setScene(mainScene);
            }
        }
    }

    private void checkForValidInput() {
        city = cityStartUpTextField.getText();
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            Matcher matcher = patternNums.matcher(city);
            boolean validInput;
            if (!matcher.find()) {
                if (!responseBodiesDailySecondAPI.containsKey(city)) {
                    try {
                        if (forecastAPI == null) {
                            forecastAPI = new ForecastAPI();
                        }
                        responseBodyCheckForValidInput = forecastAPI.httpResponseDailyForecast(city);
                        if (responseBodyCheckForValidInput != null) {
                            responseBodiesDailySecondAPI.put(city, Objects.requireNonNull(responseBodyCheckForValidInput));
                        } else {
                            throw new RuntimeException();
                        }
                    } catch (IOException | RuntimeException e) {
                        e.printStackTrace(System.out);
                    }
                } else {
                    responseBodyCheckForValidInput = responseBodiesDailySecondAPI.get(city);
                }
                validInput = isValidInput(responseBodyCheckForValidInput);
            } else {
                validInput = false;
            }
            return validInput;
        });

        future.thenAccept(validInput -> {
            // Update the UI with the validInput result
            Platform.runLater(() -> updateUI(validInput));
        });
    }

    private boolean isValidInput(String responseBody) {
        return responseBody != null;
    }

    private void updateUI(boolean validInput) {
        if (validInput) {
            passedFirstPage = "Passed!";
            fetchAndDisplayWeatherData(cityStartUpTextField.getText());
        } else {
            invalidInput.setText("Enter valid city or country");
            invalidInput.setStyle("-fx-text-fill: red;");
            cityStartUpTextField.setStyle("-fx-text-fill: red;");
            cityStartUpTextField.deselect();
            Platform.runLater(() -> cityStartUpTextField.positionCaret(cityStartUpTextField.getText().length()));
        }
    }

    private void createImage(String imageUrl) {
        if (!imageCache.containsKey(imageUrl)) {
            CompletableFuture<Image> imageFuture = loadImageAsync(imageUrl);
            imageFuture.thenAcceptAsync(image -> {
                if (image != null) {
                    // Image loaded successfully, update the UI with the image
                    imageCache.put(image.getUrl(), image);
                    Platform.runLater(() -> {
                        iconView.setImage(image);
                        iconView.setFitWidth(32);
                        iconView.setFitHeight(32);
                    });
                } else {
                    throw new RuntimeException("Image is null");
                }
            });
        } else {
            image = imageCache.get(imageUrl);
            iconView.setImage(image);
            iconView.setFitWidth(32);
            iconView.setFitHeight(32);
        }
    }

    private CompletableFuture<Image> loadImageAsync(String imageUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new Image(imageUrl);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                return null;
            }
        });
    }

    private void hideAllNodes() {
        localTimeLabel.setTextFill(Color.RED);
        inputTextField.setStyle("-fx-text-fill: red;");
        showWeeklyForecastButton.setVisible(false);
        temperatureLabel.setVisible(false);
        descriptionLabel.setVisible(false);
        temperatureFeelsLikeLabel.setVisible(false);
        convertTemperature.setVisible(false);
        showMoreWeatherInfo.setVisible(false);
        humidityLabel.setVisible(false);
        humidityLabel.setText("");
        windSpeedLabel.setVisible(false);
        convertWindSpeed.setVisible(false);
        uvLabel.setVisible(false);
        getDailyForecast.setVisible(false);

        dateForecast.setVisible(false);
        dateForecast.setText("");
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

    private void updateButtonsData() {
        convertTemperature.setForecastData(weatherData);
        convertWindSpeed.setWeatherData(weatherData);
        showMoreWeatherInfo.setForecastData(weatherData);
        showWeeklyForecastButton.setCity(city);
        showWeeklyForecastButton.setForecastAPI(forecastAPI);
        dynamicBackground.setForecastData(weatherData);
    }

    public static String getUvOutputFormat(double uvIndex) {
        if (uvIndex <= 2) {
            return "Low";
        } else if (uvIndex <= 5) {
            return "Moderate";
        } else if (uvIndex <= 7) {
            return "High";
        } else if (uvIndex <= 10) {
            return "Very High";
        } else {
            return "Extreme";
        }
    }

    private void updateAPIData() throws ParseException, IOException {
        LocalTime time = LocalTime.now();
        String timeFormat = (time.toString().split("\\.")[0].substring(0, 5));
        System.out.printf("Updated %d APIs at %s!\n", responseBodiesDailySecondAPI.size(), timeFormat);
        responseBodiesDailySecondAPI.entrySet().forEach(entry -> {
            try {
                entry.setValue(forecastAPI.httpResponseDailyForecast(entry.getKey()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String getWeatherCondition() {

        String weatherConditionAndIcon = "";
        String responseBodyDailyForecast;
        try {

            if (responseBodiesDailySecondAPI.containsKey(city)) {
                responseBodyDailyForecast = responseBodiesDailySecondAPI.get(city);
            } else {
                responseBodyDailyForecast = forecastAPI.httpResponseDailyForecast(city);
            }
            if (isValidResponse(responseBodyDailyForecast)) {
                responseBodiesDailySecondAPI.put(city, responseBodyDailyForecast);

                weatherData = objectMapper.readValue(responseBodyDailyForecast, WeatherData.class);
                weatherConditionAndIcon = weatherData.getCurrent().getCondition().getIcon() + "&" +
                        weatherData.getCurrent().getCondition().getText();
            }
        } catch (IOException exception) {
            throw new RuntimeException();
        }

        return weatherConditionAndIcon;
    }

    private static boolean isValidResponse(String responseBody) {
        return responseBody != null;

    }

    public static String formatDateToDayAndHour(String inputDateTime) {

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String formattedDate = null;
        try {
            Date date = inputFormat.parse(inputDateTime);

            // Create object for formatting the output
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, hh:mm a");

            // Format the Date object as Day of the week and Time
            formattedDate = outputFormat.format(date);

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return formattedDate;
    }
}