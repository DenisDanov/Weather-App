package com.example.weatherapp;

import com.example.weatherapp.buttons.*;
import com.example.weatherapp.dynamicBackground.DynamicBackgroundImpl;
import com.example.weatherapp.labels.BubbleLabels;
import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import parsingWeatherData.*;
import weatherApi.ForecastAPI;
import weatherApi.WeatherAppAPI;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
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
    private static ConcurrentHashMap<String, String> responseBodiesSecondAPI;
    private String lastEnteredCity;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private GridPane gridPane = new GridPane();
    private ImageView iconView = new ImageView();
    private String responseBodySecondAPI;
    private String responseBodyCheckForValidInput;
    public static String passedFirstPage;
    private DynamicBackgroundImpl dynamicBackground;

    public Main() {
        this.weatherAppAPI = new WeatherAppAPI();
        this.responseBodiesFirstAPI = new ConcurrentHashMap<>();
        responseBodiesSecondAPI = new ConcurrentHashMap<>();
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
        Platform.runLater(() -> {
            rootLayout = createRootLayout();
            mainScene = new Scene(rootLayout, 866, 700);
            stage = primaryStage;
            addStyleSheet(mainScene);
            configurePrimaryStage(primaryStage, mainScene);
            configureStartUpScene();
            setUpDynamicBackground();
            configureFetchButton();
            primaryStage.show();
            updateReturnButtonNodes();
        });
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
    }

    private void setUpDynamicBackground() {
        dynamicBackground = new DynamicBackgroundImpl(
                rootLayout,
                root,
                city,
                responseBodiesSecondAPI,
                stage,
                mainScene
        );

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
        executorService.scheduleAtFixedRate(task, 0, 5, TimeUnit.MINUTES);
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
                responseBodiesSecondAPI,
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

    private void configureStartUpScene() {
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
        stage.setScene(firstPageScene);
    }

    private void checkForValidInput() throws IOException {
        Matcher matcher = pattern.matcher(city);
        new Thread(() -> {
            if (matcher.find()) {
                if (!responseBodiesFirstAPI.containsKey(city)) {
                    try {
                        responseBodyCheckForValidInput = weatherAppAPI.httpResponse(city);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    responseBodiesFirstAPI.put(city, responseBodyCheckForValidInput);
                } else {
                    responseBodyCheckForValidInput = responseBodiesFirstAPI.get(city);
                }
                responseBodySecondAPI = getLocalTime();
            }
            Platform.runLater(() -> {
                if (responseBodyCheckForValidInput.equals("{\"cod\":\"400\",\"message\":\"Nothing to geocode\"}") ||
                        responseBodyCheckForValidInput.equals("{\"cod\":\"404\",\"message\":\"city not found\"}") ||
                        !matcher.find() ||
                        responseBodySecondAPI == null) {
                    invalidInput.setText("Enter valid city or country");
                    invalidInput.setStyle("-fx-text-fill: red;");
                    cityStartUpTextField.setStyle("-fx-text-fill: red;");
                } else {
                    if (!responseBodyCheckForValidInput.equals("")) {
                        try {
                            passedFirstPage = "Passed!";
                            fetchAndDisplayWeatherData(cityStartUpTextField.getText());
                        } catch (IOException | ParseException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        invalidInput.setText("Enter valid city or country");
                        invalidInput.setStyle("-fx-text-fill: red;");
                        cityStartUpTextField.setStyle("-fx-text-fill: red;");
                    }
                }
            });
        }).start();
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

    private void configurePrimaryStage(Stage primaryStage, Scene scene) {
        primaryStage.setTitle("Weather App");
        primaryStage.setScene(scene);
    }

    private void configureFetchButton() {
        fetchButton.setOnAction(event -> {
            try {
                // Fetch and display weather data
                if (stage.getScene() == firstPageScene) {
                    fetchAndDisplayWeatherData(cityStartUpTextField.getText());
                } else {
                    fetchAndDisplayWeatherData(inputTextField.getText());
                }
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void fetchAndDisplayWeatherData(String cityTextField) throws IOException, ParseException {
        // Fetch and display weather data logic
        city = cityTextField;
        if (stage.getScene() == firstPageScene && !passedFirstPage.equals("Passed!")) {
            try {
                checkForValidInput();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            // Perform network operations, JSON parsing, and data processing

            new Thread(() -> {
                String weatherConditionAndIcon = getWeatherCondition();
                String responseBody;
                ForecastData forecastData;

                if (!responseBodiesFirstAPI.containsKey(city)) {
                    try {
                        responseBody = weatherAppAPI.httpResponse(city);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    forecastData = getDailyForecast();
                    Gson gson = new Gson();
                    weatherData = gson.fromJson(responseBody, WeatherData.class);
                } else {
                    responseBody = responseBodiesFirstAPI.get(city);
                    Gson gson = new Gson();
                    weatherData = gson.fromJson(responseBody, WeatherData.class);
                    forecastData = getDailyForecast();
                }

                Platform.runLater(() -> {
                    if (!responseBody.equals("{\"cod\":\"400\",\"message\":\"Nothing to geocode\"}") &&
                            !responseBody.equals("{\"cod\":\"404\",\"message\":\"city not found\"}") &&
                            weatherData != null &&
                            forecastData != null) {
                        updateButtonsData();
                    }

                    if (!responseBody.equals("{\"cod\":\"400\",\"message\":\"Nothing to geocode\"}") &&
                            !responseBody.equals("{\"cod\":\"404\",\"message\":\"city not found\"}") &&
                            !city.equals(lastEnteredCity) &&
                            weatherData != null &&
                            forecastData != null) {
                        dynamicBackground.switchVideoBackground(weatherConditionAndIcon.split("&")[1]);
                    }
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
                            mainInfo = weatherData.getMain();
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

                                String iconUrl = weatherConditionAndIcon.split("&")[0];
                                String completeIconUrl = "https:" + iconUrl;
                                Image image = new Image(completeIconUrl);
                                iconView = new ImageView(image);
                                iconView.setFitWidth(32);
                                iconView.setFitHeight(32);

                                if (gridPane.getChildren().size() == 2) {
                                    gridPane.getChildren().remove(1);
                                    gridPane.add(iconView, 1, 0);
                                } else {
                                    gridPane.add(iconView, 1, 0);
                                }

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
                                        chanceOfRainingForecast.setText(String.format("Chance of raining: %d %%",
                                                forecastData.getPercentChanceOfRain()));
                                        chanceOfSnowForecast.setText(String.format("Chance of snowing: %d %%",
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
                        e.printStackTrace();
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
                });
            }).start();
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
        dynamicBackground.setResponseBodiesSecondAPI(responseBodiesSecondAPI);
        dynamicBackground.setStage(stage);
        dynamicBackground.setMainScene(mainScene);
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
        new Thread(() -> {
            for (String cityKey : responseBodiesSecondAPI.keySet()) {
                try {
                    responseBodiesSecondAPI.replace(cityKey, ForecastAPI.httpResponseForecast(cityKey));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            LocalTime time = LocalTime.now();
            String timeFormat = (time.toString().split("\\.")[0].substring(0, 5));
            System.out.printf("Updated %d APIs at %s!\n", responseBodiesSecondAPI.size(), timeFormat);

            for (String cityKey : responseBodiesFirstAPI.keySet()) {
                try {
                    responseBodiesFirstAPI.replace(cityKey, weatherAppAPI.httpResponse(cityKey));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.printf("Updated %d APIs at %s!\n", responseBodiesFirstAPI.size(), timeFormat);
        }).start();
    }

    public static ForecastData getDailyForecast() {
        String responseBody;
        if (!responseBodiesSecondAPI.containsKey(city)) {
            try {
                responseBody = ForecastAPI.httpResponseForecast(city);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            responseBody = responseBodiesSecondAPI.get(city);
        }

        if (responseBody != null &&
                !responseBody.contains("No matching location found.") &&
                !responseBody.contains("Parameter q is missing.")
        ) {
            if (!responseBodiesSecondAPI.containsKey(city)) {
                responseBodiesSecondAPI.put(city, responseBody);
            }
            JSONObject response = new JSONObject(responseBody);

            JSONArray forecastDays = response.getJSONObject("forecast").getJSONArray("forecastday");

            JSONObject forecast = forecastDays.getJSONObject(0);

            JSONObject astroObject = forecast.getJSONObject("astro");
            String date = forecast.getString("date");
            JSONObject day = forecast.getJSONObject("day");
            JSONObject weatherConditionObject = day.getJSONObject("condition");

            double maxTempC = day.getDouble("maxtemp_c");
            double minTempC = day.getDouble("mintemp_c");
            double avgTempC = day.getDouble("avgtemp_c");
            double uvIndex = day.getDouble("uv");
            double avgHumidity = day.getDouble("avghumidity");
            int chanceOfRain = day.getInt("daily_chance_of_rain");
            int chanceOfSnow = day.getInt("daily_chance_of_snow");
            String weatherCondition = weatherConditionObject.getString("text");
            String sunRise = astroObject.getString("sunrise");
            String sunSet = astroObject.getString("sunset");

            return new ForecastData(date, maxTempC, minTempC, avgTempC, uvIndex,
                    avgHumidity, chanceOfRain, chanceOfSnow, weatherCondition, sunRise, sunSet);
        }
        return null;
    }

    public static double getUV(String city) {
        String responseBody;
        if (!responseBodiesSecondAPI.containsKey(city)) {
            try {
                responseBody = ForecastAPI.httpResponseForecast(city);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            responseBodiesSecondAPI.put(city, responseBody);
        } else {
            responseBody = responseBodiesSecondAPI.get(city);
        }

        JSONObject jsonObject = new JSONObject(responseBody);
        JSONObject currentObject = jsonObject.getJSONObject("current");

        return currentObject.getDouble("uv");
    }

    private String getWeatherCondition() {

        String responseBody;
        String weatherConditionAndIcon = "";
        if (!responseBodiesSecondAPI.containsKey(city)) {
            try {
                responseBody = ForecastAPI.httpResponseForecast(city);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            responseBody = responseBodiesSecondAPI.get(city);
        }

        if (responseBody != null &&
                !responseBody.contains("No matching location found.") &&
                !responseBody.contains("Parameter q is missing.")) {
            if (!responseBodiesSecondAPI.containsKey(city)) {
                responseBodiesSecondAPI.put(city, responseBody);
            }
            JSONObject response = new JSONObject(responseBody);
            JSONObject weatherCondition = response.getJSONObject("current").getJSONObject("condition");
            weatherConditionAndIcon = (weatherCondition.getString("icon") + "&" + weatherCondition.get("text"));
        }
        return weatherConditionAndIcon;
    }

    public static String getLocalTime() {
        String responseBody;
        if (!responseBodiesSecondAPI.containsKey(city)) {
            try {
                responseBody = ForecastAPI.httpResponseForecast(city);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            responseBody = responseBodiesSecondAPI.get(city);
        }
        if (responseBody != null &&
                !responseBody.contains("No matching location found.") &&
                !responseBody.contains("Parameter q is missing.")) {
            Gson gson = new Gson();
            ForecastAPIData forecastData = gson.fromJson(responseBody, ForecastAPIData.class);
            return forecastData.getLocation().getLocaltime();
        }
        return null;
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
            e.printStackTrace();
        }
        return formattedDate;
    }
}