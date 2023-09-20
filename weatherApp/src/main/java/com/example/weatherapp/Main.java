package com.example.weatherapp;

import com.example.weatherapp.buttons.*;
import com.example.weatherapp.dynamicBackground.DynamicBackgroundImpl;
import com.example.weatherapp.labels.BubbleLabels;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.gson.Gson;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONObject;
import parsingWeatherData.ForecastData;
import parsingWeatherData.MainParsedData;
import parsingWeatherData.WeatherData;
import parsingWeatherData.WeatherInfo;
import weatherApi.ForecastAPI;
import weatherApi.WeatherAppAPI;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application {

    private final WeatherAppAPI weatherAppAPI;
    private WeatherData weatherData;
    private static String city;
    private final TextField inputTextField = new TextField(); // Create a TextField for input
    private final BubbleLabels localTimeLabel = new BubbleLabels();
    private final BubbleLabels temperatureLabel = new BubbleLabels();
    private final BubbleLabels descriptionLabel = new BubbleLabels();
    private final BubbleLabels temperatureFeelsLikeLabel = new BubbleLabels();
    private final BubbleLabels weatherDescriptionLabel = new BubbleLabels();
    private final BubbleLabels humidityLabel = new BubbleLabels();
    private final BubbleLabels windSpeedLabel = new BubbleLabels();
    private final BubbleLabels uvLabel = new BubbleLabels();
    private ShowDailyForecast getDailyForecast;
    private final Label dateForecast = new BubbleLabels();
    private final Label maxTempForecast = new BubbleLabels();
    private final Label minTempForecast = new BubbleLabels();
    private final Label avgTempForecast = new BubbleLabels();
    private final Label maxWindForecast = new BubbleLabels();
    private final Label avgHumidityForecast = new BubbleLabels();
    private final Label chanceOfRainingForecast = new BubbleLabels();
    private final Label chanceOfSnowForecast = new BubbleLabels();
    private final Label weatherDescriptionForecast = new BubbleLabels();
    private final Label sunrise = new BubbleLabels();
    private final Label sunset = new BubbleLabels();
    private final Label cityLabel = new Label();
    private ShowWeeklyForecast showWeeklyForecastButton;
    private ShowMoreWeatherData showMoreWeatherInfo;
    private ConvertTemperature convertTemperature;
    private ConvertWindSpeed convertWindSpeed;
    private final Button fetchButton = new Button("Show current weather");
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
    private final Pattern pattern = Pattern.compile("[a-zA-Z]");
    private final ConcurrentHashMap<String, String> responseBodiesFirstAPI;
    private static ConcurrentHashMap<String, String> responseBodiesDailySecondAPI;
    private String lastEnteredCity;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private GridPane gridPane = new GridPane();
    private ImageView iconView = new ImageView();
    private String responseBodySecondAPI;
    private String responseBodyCheckForValidInput;
    public static String passedFirstPage;
    private DynamicBackgroundImpl dynamicBackground;
    private Image image;
    private final Map<String, Image> imageCache = new HashMap<>();

    public Main() {
        this.weatherAppAPI = new WeatherAppAPI();
        this.responseBodiesFirstAPI = new ConcurrentHashMap<>();
        responseBodiesDailySecondAPI = new ConcurrentHashMap<>();
        this.responseBodyCheckForValidInput = "";
        this.responseBodySecondAPI = "";
        passedFirstPage = "not passed!";
        this.lastEnteredCity = "";
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
        setUpDynamicBackground();
        updateReturnButtonNodes();
        configureFetchButton();

        startScheduledTask();
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
                city,
                responseBodiesDailySecondAPI,
                stage,
                mainScene
        );
        dynamicBackground.addVideosPaths();
    }

    public void startScheduledTask() {
        Runnable task = () -> {
            // Executes the code every 1 minute
            try {
                updateAPIData();
            } catch (ParseException | IOException e) {
                throw new RuntimeException(e);
            }
        };
        // Schedule the task to run every 5 minutes
        executorService.scheduleAtFixedRate(task, 5, 5, TimeUnit.MINUTES);
    }

    private StackPane createRootLayout() {
        rootLayout = new StackPane();
        root = new VBox();
        root.setSpacing(1.5);
        setRightMargin(inputTextField);

        iconView = new ImageView();
        gridPane = new GridPane(); // 10 is the spacing between label text and icon
        gridPane.add(weatherDescriptionLabel, 0, 0);
        gridPane.add(iconView, 1, 0);

        descriptionLabel.setGraphic(gridPane);
        descriptionLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY); // Display only the graphic

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

        convertTemperature = new ConvertTemperature(temperatureLabel, temperatureFeelsLikeLabel);
        convertTemperature.setText("Convert temperature");
        root.getChildren().add(6, convertTemperature);

        convertWindSpeed = new ConvertWindSpeed(windSpeedLabel);
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
                stage
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
                weatherData,
                convertWindSpeed,
                city
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

    private void setRightMargin(Region node) {
        Insets insets = new Insets(0, 590, 0, 0); // top, right, bottom, left
        VBox.setMargin(node, insets);
    }

    private void addStyleSheet(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/mainPage.css")).toExternalForm());
        temperatureLabel.getStyleClass().add("emoji-label"); // Apply the CSS class
        temperatureFeelsLikeLabel.getStyleClass().add("emoji-label");
        Text labelText = new Text("Enter City or Country:");
        Font boldFont = Font.font("Arial", FontWeight.BOLD, 14);
        descriptionLabel.setMinHeight(30);
        labelText.setFont(boldFont);
        labelText.setFill(Color.WHITE);
        cityLabel.setGraphic(labelText);
    }

    private void configurePrimaryStage(Stage primaryStage) {
        firstPageVbox = new VBox(5);
        firstPageVbox.setAlignment(Pos.CENTER);// Center the VBox within the scene
        firstPageVbox.setPadding(new Insets(250));

        // Add the label and text field to the VBox
        firstPageVbox.getChildren().addAll(
                cityStartUpLabel,
                cityStartUpTextField,
                fetchButton,
                invalidInput
        );
        firstPageScene = new Scene(firstPageVbox, 868, 700);
        firstPageScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/firstPage.css")).toExternalForm());
        primaryStage.setTitle("Weather App");
        primaryStage.setScene(firstPageScene);
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
    }

    private void fetchAndDisplayWeatherData(String cityTextField) {

        city = cityTextField;
        if (stage.getScene() == firstPageScene && !"Passed!".equals(passedFirstPage)) {
            checkForValidInput();
        } else {
            CompletableFuture.supplyAsync(() -> {
                String responseBody;
                ForecastData forecastData;
                String weatherConditionAndIcon = getWeatherCondition();

                if (!responseBodiesFirstAPI.containsKey(city)) {
                    try {
                        responseBody = weatherAppAPI.httpResponse(city);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    responseBody = responseBodiesFirstAPI.get(city);
                }
                try {
                    forecastData = getDailyForecast();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Gson gson = new Gson();
                weatherData = gson.fromJson(responseBody, WeatherData.class);

                return new WeatherDataAndForecast(responseBody, weatherData, forecastData, weatherConditionAndIcon);
            }).thenAcceptAsync(data -> {
                String responseBody = data.getResponseBody();
                WeatherData weatherData = data.getWeatherData();
                ForecastData forecastData = data.getForecastData();
                String weatherConditionAndIcon = data.getWeatherConditionAndIcon();

                if (!responseBody.equals("{\"cod\":\"400\",\"message\":\"Nothing to geocode\"}") &&
                        !responseBody.equals("{\"cod\":\"404\",\"message\":\"city not found\"}") &&
                        weatherData != null &&
                        forecastData != null) {
                    updateButtonsData();
                }
                CompletableFuture.supplyAsync(() -> {
                    if (!responseBody.equals("{\"cod\":\"400\",\"message\":\"Nothing to geocode\"}") &&
                            !responseBody.equals("{\"cod\":\"404\",\"message\":\"city not found\"}") &&
                            !city.equals(lastEnteredCity) &&
                            weatherData != null &&
                            forecastData != null) {
                        Platform.runLater(() -> {
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
                        });
                    }
                    return null;
                }).thenAcceptAsync(videoLoaded -> {
                    // Handle any post-processing or UI updates here
                    // Other UI updates...
                    GridPane checkButtonsPane = (GridPane) root.getChildren().get(2);

                    if (!checkButtonsPane.getChildren().contains(fetchButton)) {
                        buttonsPane.add(fetchButton, 0, 0);
                    }

                    if (localTimeLabel.getTextFill().equals(Color.RED)) {
                        localTimeLabel.setTextFill(Color.WHITE);
                    }

                    try {
                        MainParsedData mainInfo;
                        WeatherInfo[] weatherInfo;
                        Matcher matcher = pattern.matcher(city);
                        if (matcher.find()) {
                            mainInfo = Objects.requireNonNull(weatherData).getMain();
                            weatherInfo = weatherData.getWeather();
                        } else {
                            mainInfo = null;
                            weatherInfo = null;
                        }

                        if (mainInfo != null && weatherInfo != null && weatherInfo.length > 0 && forecastData != null) {
                            if (inputTextField.getStyle().equals("-fx-text-fill: red;")) {
                                inputTextField.setStyle(temperatureLabel.getStyle());
                            }
                            if (!lastEnteredCity.equals(city)) {
                                if (!responseBodiesFirstAPI.containsKey(city)) {
                                    responseBodiesFirstAPI.put(city, responseBody);
                                }
                                double temp = mainInfo.getTemp();
                                double tempFeelsLike = mainInfo.getFeels_like();
                                int humidity = mainInfo.getHumidity();

                                buttonsPane.setVisible(true);
                                convertTemperature.setVisible(true);
                                showMoreWeatherInfo.setVisible(true);
                                temperatureLabel.setVisible(true);
                                descriptionLabel.setVisible(true);
                                temperatureFeelsLikeLabel.setVisible(true);

                                double temperatureCelsius = (temp - 273.15);
                                double temperatureFeelsLikeCelsius = tempFeelsLike - 273.15;
                                localTimeLabel.setText(String.format("Local time: %s",
                                        formatDateToDayAndHour(getLocalTime())));
                                temperatureLabel.setText(String.format("Temperature: %.0f°C \uD83C\uDF21",
                                        temperatureCelsius));
                                temperatureFeelsLikeLabel.setText(String.format("Feels like: %.0f°C \uD83C\uDF21",
                                        temperatureFeelsLikeCelsius));

                                weatherDescriptionLabel.setWrapText(true);
                                weatherDescriptionLabel.setText("Weather Description: " +
                                        weatherConditionAndIcon.split("&")[1]);

                                if (!humidityLabel.getText().equals("") && humidityLabel.isVisible()) {
                                    humidityLabel.setText(String.format("Humidity: %d %%", humidity));
                                    uvLabel.setText("UV Index: " + getUvOutputFormat(getUV(city)));
                                    windSpeedLabel.setText(String.format("Wind speed: %.0f km/h",
                                            (weatherData.getWind().getSpeed() * 3.6)));

                                    if (!dateForecast.getText().equals("") && dateForecast.isVisible()) {
                                        dateForecast.setText(String.format("Date: %s", forecastData.getDate()));
                                        weatherDescriptionForecast.setText("Weather description for the day: " +
                                                forecastData.getWeatherDescription());
                                        maxTempForecast.setText(String.format("Max temperature for the day: %.0f°C",
                                                forecastData.getMaxTemp()));
                                        minTempForecast.setText(String.format("Min temperature for the day: %.0f°C",
                                                forecastData.getMinTemp()));
                                        avgTempForecast.setText(String.format("UV Index for the day: %s",
                                                getUvOutputFormat(forecastData.getUvIndex())));
                                        maxWindForecast.setText(String.format("Max wind speed for the day: %.0f km/h",
                                                forecastData.getMaxWind()));
                                        avgHumidityForecast.setText(String.format("Average humidity for the day: %.0f %%",
                                                forecastData.getAvgHumidity()));
                                        chanceOfRainingForecast.setText(String.format("Chance of raining: %.0f %%",
                                                forecastData.getPercentChanceOfRain()));
                                        chanceOfSnowForecast.setText(String.format("Chance of snowing: %.0f %%",
                                                forecastData.getPercentChanceOfSnow()));
                                        sunrise.setText("Sunrise: " + forecastData.getSunRise());
                                        sunset.setText("Sunset: " + forecastData.getSunSet());
                                    } else {
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
                            if (!lastEnteredCity.equals(city)) {
                                lastEnteredCity = city;
                            }
                        } else {
                            localTimeLabel.setText("Invalid place.");
                            hideAllNodes();
                        }
                    } catch (Exception e) {
                        e.printStackTrace(System.out);
                        localTimeLabel.setText("An error occurred.");
                        hideAllNodes();
                    }
                    if (inputTextField.getText().equals("") && stage.getScene() == firstPageScene) {
                        inputTextField.setText(cityStartUpTextField.getText());
                        inputTextField.deselect();
                        Platform.runLater(() -> inputTextField.positionCaret(cityStartUpTextField.getText().length()));
                    }
                    if (stage.getScene() != mainScene) {
                        stage.setScene(mainScene);
                    }
                }, Platform::runLater);
            });
        }
    }

    private void checkForValidInput() {
        ExecutorService executor = Executors.newCachedThreadPool();
        city = cityStartUpTextField.getText();
        executor.submit(() -> {
            Matcher matcher = pattern.matcher(city);
            boolean validInput;
            if (matcher.find()) {
                if (!responseBodiesFirstAPI.containsKey(city)) {
                    try {
                        responseBodyCheckForValidInput = weatherAppAPI.httpResponse(city);
                        responseBodiesFirstAPI.put(city, responseBodyCheckForValidInput);
                    } catch (IOException e) {
                        // Handle the exception properly, e.g., log it
                    }
                } else {
                    responseBodyCheckForValidInput = responseBodiesFirstAPI.get(city);
                }
                responseBodySecondAPI = getLocalTime();

                validInput = isValidInput(matcher, responseBodyCheckForValidInput, responseBodySecondAPI);
            } else {
                validInput = false;
            }

            // Update the UI with the validInput result
            Platform.runLater(() -> updateUI(validInput));
        });
        executor.shutdown();
    }

    private boolean isValidInput(Matcher matcher, String responseBody, String localTime) {
        return !responseBody.equals("{\"cod\":\"400\",\"message\":\"Nothing to geocode\"}") &&
                !responseBody.equals("{\"cod\":\"404\",\"message\":\"city not found\"}") &&
                matcher.find() &&
                localTime != null;
    }

    private void updateUI(boolean validInput) {
        if (validInput) {
            passedFirstPage = "Passed!";
            fetchAndDisplayWeatherData(cityStartUpTextField.getText());
        } else {
            invalidInput.setText("Enter valid city or country");
            invalidInput.setStyle("-fx-text-fill: red;");
            cityStartUpTextField.setStyle("-fx-text-fill: red;");
        }
    }

    private void createImage(String completeIconUrl) {

        if (!imageCache.containsKey(completeIconUrl)) {
            image = new Image(completeIconUrl);
            iconView = new ImageView(image);
            iconView.setFitWidth(32);
            iconView.setFitHeight(32);
            imageCache.put(completeIconUrl, image);
        } else {
            image = imageCache.get(completeIconUrl);
            iconView = new ImageView(image);
            iconView.setFitWidth(32);
            iconView.setFitHeight(32);
        }
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
        convertTemperature.setWeatherData(weatherData);
        convertWindSpeed.setWeatherData(weatherData);
        showMoreWeatherInfo.setWeatherData(weatherData);
        showMoreWeatherInfo.setCity(city);
        showWeeklyForecastButton.setCity(city);
        dynamicBackground.setCity(city);
        dynamicBackground.setResponseBodiesSecondAPI(responseBodiesDailySecondAPI);
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
        responseBodiesFirstAPI.entrySet().forEach(entry -> {
            try {
                entry.setValue(weatherAppAPI.httpResponse(entry.getKey()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        LocalTime time = LocalTime.now();
        String timeFormat = (time.toString().split("\\.")[0].substring(0, 5));
        System.out.printf("Updated %d APIs at %s!\n", responseBodiesDailySecondAPI.size(), timeFormat);
        responseBodiesDailySecondAPI.entrySet().forEach(entry -> {
            try {
                entry.setValue(ForecastAPI.httpResponseDailyForecast(entry.getKey()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.printf("Updated %d APIs at %s!\n", responseBodiesFirstAPI.size(), timeFormat);
    }

    public static ForecastData getDailyForecast() throws IOException {
        String responseBodyDailyForecast;
        if (!responseBodiesDailySecondAPI.containsKey(city)) {
            responseBodyDailyForecast = ForecastAPI.httpResponseDailyForecast(city);
        } else {
            responseBodyDailyForecast = responseBodiesDailySecondAPI.get(city);
        }
        if (responseBodyDailyForecast != null &&
                !responseBodyDailyForecast.contains("No matching location found.") &&
                !responseBodyDailyForecast.contains("Parameter q is missing.")
        ) {
            if (!responseBodiesDailySecondAPI.containsKey(city)) {
                responseBodiesDailySecondAPI.put(city, responseBodyDailyForecast);
            }
            JsonFactory jsonFactory = new JsonFactory();
            try (JsonParser jsonParser = jsonFactory.createParser(responseBodyDailyForecast)) {
                while (jsonParser.nextToken() != null) {
                    if (jsonParser.getCurrentToken() == JsonToken.FIELD_NAME) {
                        String fieldName = jsonParser.getCurrentName();
                        jsonParser.nextToken(); // Move to the value token

                        if ("forecastday".equals(fieldName)) {
                            // Start processing the forecastday object
                            String date;
                            double maxTempC = 0;
                            double minTempC = 0;
                            double maxwindKph = 0;
                            double uvIndex = 0;
                            double chanceOfRain = 0;
                            double chanceOfSnow = 0;
                            String weatherCondition = "";
                            double avgHumidity = 0;
                            String sunRise = "";
                            String sunSet = "";
                            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                                if (jsonParser.getCurrentToken() == JsonToken.FIELD_NAME) {
                                    String forecastField = jsonParser.getCurrentName();
                                    jsonParser.nextToken(); // Move to the value token

                                    if ("date".equals(forecastField)) {
                                        date = jsonParser.getText();
                                        while (sunSet.equals("")) {
                                            switch (jsonParser.getText()) {
                                                case "maxtemp_c":
                                                    jsonParser.nextToken();
                                                    maxTempC = Double.parseDouble(jsonParser.getText());
                                                    break;
                                                case "mintemp_c":
                                                    jsonParser.nextToken();
                                                    minTempC = Double.parseDouble(jsonParser.getText());
                                                    break;
                                                case "maxwind_kph":
                                                    jsonParser.nextToken();
                                                    maxwindKph = Double.parseDouble(jsonParser.getText());
                                                    break;
                                                case "avghumidity":
                                                    jsonParser.nextToken();
                                                    avgHumidity = Double.parseDouble(jsonParser.getText());
                                                    break;
                                                case "daily_chance_of_rain":
                                                    jsonParser.nextToken();
                                                    chanceOfRain = Double.parseDouble(jsonParser.getText());
                                                    break;
                                                case "daily_chance_of_snow":
                                                    jsonParser.nextToken();
                                                    chanceOfSnow = Double.parseDouble(jsonParser.getText());
                                                    break;
                                                case "text":
                                                    jsonParser.nextToken();
                                                    weatherCondition = (jsonParser.getText());
                                                    break;
                                                case "uv":
                                                    jsonParser.nextToken();
                                                    uvIndex = Double.parseDouble(jsonParser.getText());
                                                    break;
                                                case "sunrise":
                                                    jsonParser.nextToken();
                                                    sunRise = jsonParser.getText();
                                                    break;
                                                case "sunset":
                                                    jsonParser.nextToken();
                                                    sunSet = jsonParser.getText();
                                                    break;
                                            }
                                            jsonParser.nextToken();
                                        }
                                        return new ForecastData(date, maxTempC, minTempC, uvIndex, maxwindKph,
                                                avgHumidity, chanceOfRain, chanceOfSnow, weatherCondition, sunRise, sunSet);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static double getUV(String city) {
        String responseBody;
        if (!responseBodiesDailySecondAPI.containsKey(city)) {
            try {
                responseBody = ForecastAPI.httpResponseDailyForecast(city);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            responseBodiesDailySecondAPI.put(city, responseBody);
        } else {
            responseBody = responseBodiesDailySecondAPI.get(city);
        }

        JSONObject jsonObject = new JSONObject(responseBody);
        JSONObject currentObject = jsonObject.getJSONObject("current");

        return currentObject.getDouble("uv");
    }

    private String getWeatherCondition() {
        String weatherConditionAndIcon = "";
        String responseBodyDailyForecast;
        try {

            if (responseBodiesDailySecondAPI.containsKey(city)) {
                responseBodyDailyForecast = responseBodiesDailySecondAPI.get(city);
            } else {
                responseBodyDailyForecast = ForecastAPI.httpResponseDailyForecast(city);
            }
            if (isValidResponse(responseBodyDailyForecast)) {
                responseBodiesDailySecondAPI.put(city, responseBodyDailyForecast);

                JsonFactory factory = new JsonFactory();
                JsonParser parser = factory.createParser(responseBodyDailyForecast);

                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String field = parser.getCurrentName();
                    parser.nextToken(); // Move to the value
                    if ("text".equals(field)) {
                        String text = parser.getText();
                        parser.nextToken();
                        parser.nextToken();
                        String icon = parser.getText();
                        weatherConditionAndIcon = icon + "&" + text;
                        break; // Found what we need, exit the loop
                    }
                }

                parser.close(); // Close the parser
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return weatherConditionAndIcon;
    }

    public static String getLocalTime() {
        String responseBodyDailyForecast;
        try {
            if (responseBodiesDailySecondAPI.containsKey(city)) {
                responseBodyDailyForecast = responseBodiesDailySecondAPI.get(city);
            } else {
                responseBodyDailyForecast = ForecastAPI.httpResponseDailyForecast(city);
                responseBodiesDailySecondAPI.put(city, Objects.requireNonNull(responseBodyDailyForecast));
            }

            if (isValidResponse(responseBodyDailyForecast)) {
                JsonFactory factory = new JsonFactory();
                JsonParser parser = factory.createParser(responseBodyDailyForecast);

                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String field = parser.getCurrentName();
                    parser.nextToken(); // Move to the value
                    if ("location".equals(field)) {
                        while (parser.nextToken() != JsonToken.END_OBJECT) {
                            parser.nextToken();
                            if ("localtime".equals(parser.getText())) {
                                parser.nextToken();
                                return parser.getText();
                            }
                        }
                    }
                }
                parser.close(); // Close the parser
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static boolean isValidResponse(String responseBody) {
        return responseBody != null &&
                !responseBody.contains("No matching location found.") &&
                !responseBody.contains("Parameter q is missing.");
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