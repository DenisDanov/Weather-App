package com.example.weatherapp;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application {
    private final WeatherAppAPI weatherAppAPI;
    private WeatherData weatherData;
    private String city;
    private final TextField inputTextField = new TextField(); // Create a TextField for input
    private final BubbleLabels temperatureLabel = new BubbleLabels();
    private final BubbleLabels descriptionLabel = new BubbleLabels();
    private final BubbleLabels temperatureFeelsLikeLabel = new BubbleLabels();
    private final BubbleLabels humidityLabel = new BubbleLabels();
    private final BubbleLabels windSpeedLabel = new BubbleLabels();
    private final BubbleLabels localTimeLabel = new BubbleLabels();
    private final BubbleLabels uvLabel = new BubbleLabels();
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
    private final Button showMoreWeatherInfo = new Button("Show more weather info");
    private final Button convertTemperature = new Button("Convert temperature");
    private final Button convertWindSpeed = new Button("Convert wind speed");
    private final Button getDailyForecast = new Button("Show daily forecast");
    private final Button fetchButton = new Button("Show current weather");
    private final Button goBackToFirstPage = new Button("Return to the first page");
    private final Label cityLabel = new Label();
    private final Button showWeeklyForecastButton = new Button("Show weekly forecast");
    private Scene mainScene;
    private StackPane rootLayout = createRootLayout();
    private VBox root;
    Stage stage;
    private VBox firstPageVbox;
    private final Label cityStartUpLabel = new Label("Enter City or Country:");
    private final Label invalidInput = new Label();
    private final TextField cityStartUpTextField = new TextField();
    private Scene firstPageScene;
    private GridPane buttonsPane;
    private final Pattern pattern = Pattern.compile("[a-zA-Z]");
    private Color originalTextColor;
    private MediaView mediaView;
    private MediaPlayer mediaPlayer;
    private final ExecutorService videoExecutor = Executors.newSingleThreadExecutor();

    public Main() {
        this.weatherAppAPI = new WeatherAppAPI();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        mainScene = new Scene(rootLayout, 868, 700);
        stage = primaryStage;
        addStyleSheet(mainScene);
        configurePrimaryStage(primaryStage, mainScene);
        configureStartUpScene();
        configureFetchButton();
        configureGoBackToFirstPageButton();
        configureConvertTemperatureButton();
        configureShowMoreButton();
        configureConvertWindSpeedButton();
        configureGetDailyForecastButton();
        configureWeeklyForecastButton();
        primaryStage.show();
    }

    private void configureStartUpScene() {
        firstPageVbox = new VBox(5);
        firstPageVbox.setAlignment(Pos.CENTER);// Center the VBox within the scene
        firstPageVbox.setPadding(new Insets(250));

        // Add the label and text field to the VBox
        firstPageVbox.getChildren().addAll(cityStartUpLabel, cityStartUpTextField, fetchButton, invalidInput);
        firstPageScene = new Scene(firstPageVbox, 868, 700);
        firstPageScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/firstPage.css")).toExternalForm());
        stage.setScene(firstPageScene);
    }

    private void checkForValidInput() throws IOException {
        Matcher matcher = pattern.matcher(city);

        String responseBody = "";
        String responseBodyN2 = "";

        if (matcher.find()) {
            responseBody = weatherAppAPI.httpResponse(city);
            responseBodyN2 = getLocalTime(city);
        }
        if (responseBody.equals("{\"cod\":\"400\",\"message\":\"Nothing to geocode\"}") ||
                responseBody.equals("{\"cod\":\"404\",\"message\":\"city not found\"}") || !matcher.find()
                || responseBodyN2 == null || responseBodyN2.contains("No matching location found.")) {
            invalidInput.setText("Enter valid city or country");
            invalidInput.setStyle("-fx-text-fill: red;");
            cityStartUpTextField.setStyle("-fx-text-fill: red;");
        } else {
            if (!responseBody.equals("")) {
                cityStartUpTextField.setStyle(inputTextField.getStyle());
                Platform.runLater(() -> {
                    inputTextField.setText(cityStartUpTextField.getText());
                    inputTextField.positionCaret(inputTextField.getText().length());
                });
                stage.setScene(mainScene);
                fetchAndDisplayWeatherData(city);
            } else {
                invalidInput.setText("Enter valid city or country");
                invalidInput.setStyle("-fx-text-fill: red;");
                cityStartUpTextField.setStyle("-fx-text-fill: red;");
            }
        }
    }

    private void setRightMargin(Region node, double margin) {
        Insets insets = new Insets(0, margin, 0, 0); // top, right, bottom, left
        VBox.setMargin(node, insets);
    }

    private StackPane createRootLayout() {
        rootLayout = new StackPane();
        root = new VBox();
        root.setSpacing(1.5);
        setRightMargin(inputTextField, 590);

        Media defaultMedia = new Media(getClass().getResource("/screen-recorder-08-28-2023-12-54-13-642_tGY0iQ7a (online-video-cutter.com) (2).mp4").toString());
        mediaPlayer = new MediaPlayer(defaultMedia);
        mediaView = new MediaView();
        mediaView.setMediaPlayer(mediaPlayer);
        rootLayout.getChildren().add(mediaView);
        // Start video playback in a separate thread
        new Thread(() -> {
            mediaPlayer.play();
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        }).start();
        buttonsPane = new GridPane();
        buttonsPane.add(fetchButton, 0, 0);
        buttonsPane.add(goBackToFirstPage, 1, 0);
        buttonsPane.setHgap(5);

        TableView<TemperatureData> temperatureTable = new TableView<>();
        temperatureTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Add the TableView to the root layout
        root.getChildren().addAll(cityLabel, inputTextField,
                buttonsPane,
                localTimeLabel,
                temperatureLabel,
                temperatureFeelsLikeLabel,
                descriptionLabel);

        root.getChildren().add(6, convertTemperature);
        root.getChildren().addAll(showMoreWeatherInfo, humidityLabel, uvLabel,
                windSpeedLabel, convertWindSpeed, getDailyForecast);
        root.getChildren().addAll(dateForecast, maxTempForecast, minTempForecast, avgTempForecast,
                maxWindForecast, avgHumidityForecast, chanceOfRainingForecast,
                chanceOfSnowForecast, weatherDescriptionForecast, sunrise, sunset, showWeeklyForecastButton);

        rootLayout.getChildren().add(root);

        showWeeklyForecastButton.setVisible(false);
        convertTemperature.setVisible(false);
        showMoreWeatherInfo.setVisible(false);
        convertWindSpeed.setVisible(false);
        uvLabel.setVisible(false);
        humidityLabel.setVisible(false);
        windSpeedLabel.setVisible(false);
        buttonsPane.setVisible(false);

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

    private void addStyleSheet(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/mainPage.css")).toExternalForm());
        temperatureLabel.getStyleClass().add("emoji-label"); // Apply the CSS class
        descriptionLabel.getStyleClass().add("emoji-label");// Apply the CSS class
        temperatureFeelsLikeLabel.getStyleClass().add("emoji-label");
        Text labelText = new Text("Enter City or Country:");
        Font boldFont = Font.font("Arial", FontWeight.BOLD, 14);
        labelText.setFont(boldFont);
        labelText.setFill(Color.WHITE);
        cityLabel.setGraphic(labelText);
    }

    private void configurePrimaryStage(Stage primaryStage, Scene scene) {
        primaryStage.setTitle("Weather App");
        primaryStage.setScene(scene);
    }

    private void configureGoBackToFirstPageButton() {
        goBackToFirstPage.setOnAction(actionEvent -> {
            stage.setScene(firstPageScene);
            invalidInput.setText("");
            if (!firstPageVbox.getChildren().contains(fetchButton)) {
                firstPageVbox.getChildren().add(2, fetchButton);
                cityStartUpTextField.setText(city);
                cityStartUpTextField.setStyle(temperatureLabel.getStyle());
            }
        });
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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void configureConvertTemperatureButton() {
        convertTemperature.setOnAction(actionEvent -> {
            // Convert temperature logic
            MainParsedData mainInfo = weatherData.getMain();
            if (temperatureLabel.getText().contains("°C") &&
                    temperatureFeelsLikeLabel.getText().contains("°C")) {
                temperatureLabel.setText(String.format("Temperature: %.0f°F \uD83C\uDF21", getTempInFahrenheit(mainInfo.getTemp())));
                temperatureFeelsLikeLabel.setText(String.format("Feels like: %.0f°F \uD83C\uDF21", getTempInFahrenheit(mainInfo.getFeels_like())));
            } else if (temperatureLabel.getText().contains("°F") &&
                    temperatureFeelsLikeLabel.getText().contains("°F")) {
                temperatureLabel.setText(String.format("Temperature: %.0f K \uD83C\uDF21", (mainInfo.getTemp())));
                temperatureFeelsLikeLabel.setText(String.format("Feels like: %.0f K \uD83C\uDF21", (mainInfo.getFeels_like())));
            } else {
                temperatureLabel.setText(String.format("Temperature: %.0f°C \uD83C\uDF21", getTempInCelsius(mainInfo.getTemp())));
                temperatureFeelsLikeLabel.setText(String.format("Feels like: %.0f°C \uD83C\uDF21", getTempInCelsius(mainInfo.getFeels_like())));
            }
        });
    }

    private void configureShowMoreButton() {
        showMoreWeatherInfo.setOnAction(actionEvent -> {
            // Show more weather info logic
            configureShowMoreButtonAction();
        });
    }

    private void configureWeeklyForecastButton() {
        showWeeklyForecastButton.setOnAction(actionEvent -> configureWeeklyForecastButtonAction());
    }

    private void configureWeeklyForecastButtonAction() {
        if (root.getChildren().get(0).equals(cityLabel)) {
            resetUI();
            TableView<TemperatureData> tableView = new TableView<>();

            // Create columns for days of the week
            TableColumn<TemperatureData, String> dayColumn = new TableColumn<>("Day");
            dayColumn.setCellValueFactory(new PropertyValueFactory<>("day"));
            tableView.getColumns().add(dayColumn);
            dayColumn.setCellFactory(column -> new TableCell<>() {
                private final Text text;

                {
                    text = new Text();
                    text.wrappingWidthProperty().bind(dayColumn.widthProperty().subtract(4.8));
                    setGraphic(text);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        text.setText(item);
                        setGraphic(text);
                    }
                }
            });

            String[] daysOfWeek = new String[7];
            JSONArray weeklyForecast = getWeeklyForecast();
            JSONObject[] daysOfTheWeek = new JSONObject[7];
            String day1FromForecast = "";
            for (int i = 0; i < weeklyForecast.length(); i++) {
                JSONObject day = weeklyForecast.getJSONObject(i);
                if (i == 0) {
                    day1FromForecast = day.get("date").toString();
                }
                daysOfWeek[i] = formatDateTime(day.get("date").toString());
                daysOfTheWeek[i] = weeklyForecast.getJSONObject(i);
            }
            // Add day columns
            for (String day : daysOfWeek) {
                TableColumn<TemperatureData, String> columns = new TableColumn<>(day);
                columns.setCellValueFactory(new PropertyValueFactory<>(day.toLowerCase())); // Matches with the property name in TemperatureData
                columns.setResizable(true);
                columns.setCellFactory(column -> new TableCell<>() {
                    private final Text text;

                    {
                        text = new Text();
                        text.wrappingWidthProperty().bind(columns.widthProperty().subtract(4));
                        setGraphic(text);
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            text.setText(item);
                            setGraphic(text);
                        }
                    }
                });
                tableView.getColumns().add(columns);
            }
            ForecastData day = getDailyForecast();
            JSONObject day1 = daysOfTheWeek[0];
            JSONObject day2 = daysOfTheWeek[1];
            JSONObject day3 = daysOfTheWeek[2];
            JSONObject day4 = daysOfTheWeek[3];
            JSONObject day5 = daysOfTheWeek[4];
            JSONObject day6 = daysOfTheWeek[5];
            JSONObject day7 = daysOfTheWeek[6];

            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            System.out.println(currentDate.format(dateFormat));
            System.out.println(day1FromForecast);

            if (currentDate.format(dateFormat).equals(day1FromForecast)) {
                tableView.getItems().add(new TemperatureData("Max Temperature",
                        (day6.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                        (day7.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                        (day1.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                        (day2.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                        (day3.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                        (day4.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                        (day5.getJSONObject("day").getDouble("maxtemp_c") + "°C")));
                tableView.getItems().add(new TemperatureData("Min Temperature",
                        (day6.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                        (day7.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                        (day1.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                        (day2.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                        (day3.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                        (day4.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                        (day5.getJSONObject("day").getDouble("mintemp_c") + "°C")));
                tableView.getItems().add(new TemperatureData("Avg Temperature",
                        (day6.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                        (day7.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                        (day1.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                        (day2.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                        (day3.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                        (day4.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                        (day5.getJSONObject("day").getDouble("avgtemp_c") + "°C")));
                tableView.getItems().add(new TemperatureData("Max Wind Speed",
                        (day6.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                        (day7.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                        (day1.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                        (day2.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                        (day3.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                        (day4.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                        (day5.getJSONObject("day").getDouble("maxwind_kph") + " km/h")));
                tableView.getItems().add(new TemperatureData("Avg Humidity",
                        (day6.getJSONObject("day").getDouble("avghumidity") + "%"),
                        (day7.getJSONObject("day").getDouble("avghumidity") + "%"),
                        (day1.getJSONObject("day").getDouble("avghumidity") + "%"),
                        (day2.getJSONObject("day").getDouble("avghumidity") + "%"),
                        (day3.getJSONObject("day").getDouble("avghumidity") + "%"),
                        (day4.getJSONObject("day").getDouble("avghumidity") + "%"),
                        (day5.getJSONObject("day").getDouble("avghumidity") + "%")));
                tableView.getItems().add(new TemperatureData("UV Index",
                        getUvOutputFormat(day6.getJSONObject("day").getDouble("uv")),
                        getUvOutputFormat(day7.getJSONObject("day").getDouble("uv")),
                        getUvOutputFormat(day1.getJSONObject("day").getDouble("uv")),
                        getUvOutputFormat(day2.getJSONObject("day").getDouble("uv")),
                        getUvOutputFormat(day3.getJSONObject("day").getDouble("uv")),
                        getUvOutputFormat(day4.getJSONObject("day").getDouble("uv")),
                        getUvOutputFormat(day5.getJSONObject("day").getDouble("uv"))));
                tableView.getItems().add(new TemperatureData("Chance of Rain",
                        (day6.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                        (day7.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                        (day1.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                        (day2.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                        (day3.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                        (day4.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                        (day5.getJSONObject("day").getDouble("daily_chance_of_rain") + "%")));
                tableView.getItems().add(new TemperatureData("Chance of Snow",
                        (day6.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                        (day7.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                        (day1.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                        (day2.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                        (day3.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                        (day4.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                        (day5.getJSONObject("day").getDouble("daily_chance_of_snow") + "%")));
                tableView.getItems().add(new TemperatureData("Weather Description",
                        (day6.getJSONObject("day").getJSONObject("condition").getString("text")),
                        (day7.getJSONObject("day").getJSONObject("condition").getString("text")),
                        (day1.getJSONObject("day").getJSONObject("condition").getString("text")),
                        (day2.getJSONObject("day").getJSONObject("condition").getString("text")),
                        (day3.getJSONObject("day").getJSONObject("condition").getString("text")),
                        (day4.getJSONObject("day").getJSONObject("condition").getString("text")),
                        (day5.getJSONObject("day").getJSONObject("condition").getString("text"))));
                tableView.getItems().add(new TemperatureData("Sunrise",
                        (day6.getJSONObject("astro").getString("sunrise")),
                        (day7.getJSONObject("astro").getString("sunrise")),
                        (day1.getJSONObject("astro").getString("sunrise")),
                        (day2.getJSONObject("astro").getString("sunrise")),
                        (day3.getJSONObject("astro").getString("sunrise")),
                        (day4.getJSONObject("astro").getString("sunrise")),
                        (day5.getJSONObject("astro").getString("sunrise"))));
                tableView.getItems().add(new TemperatureData("Sunset",
                        (day6.getJSONObject("astro").getString("sunset")),
                        (day7.getJSONObject("astro").getString("sunset")),
                        (day1.getJSONObject("astro").getString("sunset")),
                        (day2.getJSONObject("astro").getString("sunset")),
                        (day3.getJSONObject("astro").getString("sunset")),
                        (day4.getJSONObject("astro").getString("sunset")),
                        (day5.getJSONObject("astro").getString("sunset"))));
            } else {
                tableView.getItems().add(new TemperatureData("Max Temperature",
                        (day7.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                        (day1.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                        (day2.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                        (day3.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                        (day4.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                        (day5.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                        (day6.getJSONObject("day").getDouble("maxtemp_c") + "°C")));
                tableView.getItems().add(new TemperatureData("Min Temperature",
                        (day7.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                        (day1.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                        (day2.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                        (day3.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                        (day4.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                        (day5.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                        (day6.getJSONObject("day").getDouble("mintemp_c") + "°C")));
                tableView.getItems().add(new TemperatureData("Avg Temperature",
                        (day7.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                        (day1.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                        (day2.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                        (day3.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                        (day4.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                        (day5.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                        (day6.getJSONObject("day").getDouble("avgtemp_c") + "°C")));
                tableView.getItems().add(new TemperatureData("Max Wind Speed",
                        (day7.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                        (day1.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                        (day2.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                        (day3.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                        (day4.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                        (day5.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                        (day6.getJSONObject("day").getDouble("maxwind_kph") + " km/h")));
                tableView.getItems().add(new TemperatureData("Avg Humidity",
                        (day7.getJSONObject("day").getDouble("avghumidity") + "%"),
                        (day1.getJSONObject("day").getDouble("avghumidity") + "%"),
                        (day2.getJSONObject("day").getDouble("avghumidity") + "%"),
                        (day3.getJSONObject("day").getDouble("avghumidity") + "%"),
                        (day4.getJSONObject("day").getDouble("avghumidity") + "%"),
                        (day5.getJSONObject("day").getDouble("avghumidity") + "%"),
                        (day6.getJSONObject("day").getDouble("avghumidity") + "%")));
                tableView.getItems().add(new TemperatureData("UV Index",
                        getUvOutputFormat(day7.getJSONObject("day").getDouble("uv")),
                        getUvOutputFormat(day1.getJSONObject("day").getDouble("uv")),
                        getUvOutputFormat(day2.getJSONObject("day").getDouble("uv")),
                        getUvOutputFormat(day3.getJSONObject("day").getDouble("uv")),
                        getUvOutputFormat(day4.getJSONObject("day").getDouble("uv")),
                        getUvOutputFormat(day5.getJSONObject("day").getDouble("uv")),
                        getUvOutputFormat(day6.getJSONObject("day").getDouble("uv"))));
                tableView.getItems().add(new TemperatureData("Chance of Rain",
                        (day7.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                        (day1.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                        (day2.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                        (day3.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                        (day4.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                        (day5.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                        (day6.getJSONObject("day").getDouble("daily_chance_of_rain") + "%")));
                tableView.getItems().add(new TemperatureData("Chance of Snow",
                        (day7.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                        (day1.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                        (day2.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                        (day3.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                        (day4.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                        (day5.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                        (day6.getJSONObject("day").getDouble("daily_chance_of_snow") + "%")));
                tableView.getItems().add(new TemperatureData("Weather Description",
                        (day7.getJSONObject("day").getJSONObject("condition").getString("text")),
                        (day1.getJSONObject("day").getJSONObject("condition").getString("text")),
                        (day2.getJSONObject("day").getJSONObject("condition").getString("text")),
                        (day3.getJSONObject("day").getJSONObject("condition").getString("text")),
                        (day4.getJSONObject("day").getJSONObject("condition").getString("text")),
                        (day5.getJSONObject("day").getJSONObject("condition").getString("text")),
                        (day6.getJSONObject("day").getJSONObject("condition").getString("text"))));
                tableView.getItems().add(new TemperatureData("Sunrise",
                        (day7.getJSONObject("astro").getString("sunrise")),
                        (day1.getJSONObject("astro").getString("sunrise")),
                        (day2.getJSONObject("astro").getString("sunrise")),
                        (day3.getJSONObject("astro").getString("sunrise")),
                        (day4.getJSONObject("astro").getString("sunrise")),
                        (day5.getJSONObject("astro").getString("sunrise")),
                        (day6.getJSONObject("astro").getString("sunrise"))));
                tableView.getItems().add(new TemperatureData("Sunset",
                        (day7.getJSONObject("astro").getString("sunset")),
                        (day1.getJSONObject("astro").getString("sunset")),
                        (day2.getJSONObject("astro").getString("sunset")),
                        (day3.getJSONObject("astro").getString("sunset")),
                        (day4.getJSONObject("astro").getString("sunset")),
                        (day5.getJSONObject("astro").getString("sunset")),
                        (day6.getJSONObject("astro").getString("sunset"))));
            }

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            double tableViewWidth = 0;
            for (TableColumn<?, ?> column : tableView.getColumns()) {
                tableViewWidth += column.getWidth();
            }
            tableView.setPrefHeight(440);
            VBox root = new VBox(tableView);
            Scene scene = new Scene(root, tableViewWidth, 650); // Adjusted scene dimensions
            Button getToMainPage = new Button("Return to the main page");
            root.getChildren().add(getToMainPage);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/weeklyForecastPage.css")).toExternalForm());
            stage.setScene(scene);

            stage.show();

            getToMainPage.setOnAction(actionEvent -> returnToMainPage());
        } else {
            returnToMainPage();
        }
    }

    public static class TemperatureData {
        private final String day;
        private final String monday;
        private final String tuesday;
        private final String wednesday;
        private final String thursday;
        private final String friday;
        private final String saturday;
        private final String sunday;

        public TemperatureData(String day, String day1, String day2, String day3, String day4, String day5, String day6, String day7) {
            this.day = day;
            this.monday = day1;
            this.tuesday = day2;
            this.wednesday = day3;
            this.thursday = day4;
            this.friday = day5;
            this.saturday = day6;
            this.sunday = day7;
        }

        public String getDay() {
            return day;
        }

        public String getMonday() {
            return monday;
        }

        public String getTuesday() {
            return tuesday;
        }

        public String getWednesday() {
            return wednesday;
        }

        public String getThursday() {
            return thursday;
        }

        public String getFriday() {
            return friday;
        }

        public String getSaturday() {
            return saturday;
        }

        public String getSunday() {
            return sunday;
        }
    }

    private void configureConvertWindSpeedButton() {
        convertWindSpeed.setOnAction(actionEvent -> {
            // Convert wind speed logic
            if (windSpeedLabel.getText().contains("km/h")) {
                windSpeedLabel.setText(String.format("Wind speed: %.2f mph", getWindSpeedInMiles(weatherData.getWind().getSpeed())));
            } else if (windSpeedLabel.getText().contains("mph")) {
                windSpeedLabel.setText(String.format("Wind speed: %.2f km/h", getWindSpeedInKms(weatherData.getWind().getSpeed())));
            }
        });
    }

    private void configureGetDailyForecastButton() {
        getDailyForecast.setOnAction(actionEvent -> {
            // Get daily forecast logic
            new Thread(() -> {
                // Perform network operations, JSON parsing, and data processing here
                ForecastData forecastData = getDailyForecast();
                Platform.runLater(() -> {
                    if (dateForecast.getText().equals("") && !dateForecast.isVisible()) {
                        if (!showWeeklyForecastButton.isVisible()) {
                            showWeeklyForecastButton.setVisible(true);
                        }
                        dateForecast.setText(String.format("Date: %s", forecastData.getDate()));
                        weatherDescriptionForecast.setText("Weather description for the day: " + forecastData.getWeatherDescription());
                        maxTempForecast.setText(String.format("Max temperature for the day: %f°C", forecastData.getMaxTemp()));
                        minTempForecast.setText(String.format("Min temperature for the day: %.0f°C", forecastData.getMinTemp()));
                        avgTempForecast.setText(String.format("Average temperature for the day: %.0f°C", forecastData.getAvgTemp()));
                        maxWindForecast.setText(String.format("Max wind speed for the day: %.2f km/h", forecastData.getMaxWind()));
                        avgHumidityForecast.setText("Average humidity for the day: " + forecastData.getAvgHumidity() + "%");
                        chanceOfRainingForecast.setText(String.format("Chance of raining: %d%%", forecastData.getPercentChanceOfRain()));
                        chanceOfSnowForecast.setText(String.format("Chance of snowing: %d%%", forecastData.getPercentChanceOfSnow()));
                        sunrise.setText("Sunrise: " + forecastData.getSunRise());
                        sunset.setText("Sunset: " + forecastData.getSunSet());
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
                    } else {
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
                });
            }).start();
        });

    }

    private void fetchAndDisplayWeatherData(String cityTextField) throws IOException {
        // Fetch and display weather data logic
        this.city = cityTextField;
        if (stage.getScene() == firstPageScene) {
            try {
                checkForValidInput();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            GridPane checkButtonsPane = (GridPane) root.getChildren().get(2);
            if (!checkButtonsPane.getChildren().contains(fetchButton)) {
                buttonsPane.add(fetchButton, 0, 0);
            }
            if (localTimeLabel.getTextFill().equals(Color.RED)) {
                localTimeLabel.setTextFill(originalTextColor);
            }
            Button convertButton = convertTemperature;
            Button showMoreButton = showMoreWeatherInfo;
            Button convertWindSpeedButton = convertWindSpeed;
            new Thread(() -> {
                // Perform network operations, JSON parsing, and data processing here
                String responseBody = null;
                try {
                    responseBody = weatherAppAPI.httpResponse(city);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ForecastData forecastData = getDailyForecast();
                String finalResponseBody = responseBody;
                Gson gson = new Gson();
                weatherData = gson.fromJson(finalResponseBody, WeatherData.class);
                System.out.println(finalResponseBody);
                Platform.runLater(() -> {
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
                        if (mainInfo != null && weatherInfo != null && weatherInfo.length > 0 && getLocalTime(city) != null) {
                            if (inputTextField.getStyle().equals("-fx-text-fill: red;")) {
                                inputTextField.setStyle(temperatureLabel.getStyle());
                            }
                            double temp = mainInfo.getTemp();
                            double tempFeelsLike = mainInfo.getFeels_like();
                            String description = weatherInfo[0].getDescription();
                            int humidity = mainInfo.getHumidity();

                            // Update your labels here
                            buttonsPane.setVisible(true);
                            convertButton.setVisible(true);
                            showMoreButton.setVisible(true);
                            temperatureLabel.setVisible(true);
                            descriptionLabel.setVisible(true);
                            temperatureFeelsLikeLabel.setVisible(true);

                            double temperatureCelsius = getTempInCelsius(temp);
                            double temperatureFeelsLikeCelsius = getTempInCelsius(tempFeelsLike);
                            localTimeLabel.setText(String.format("Local time: %s", formatDateToDayAndHour(getLocalTime(city))));
                            originalTextColor = (Color) localTimeLabel.getTextFill();
                            temperatureLabel.setText(String.format("Temperature: %.0f°C \uD83C\uDF21", temperatureCelsius));
                            temperatureFeelsLikeLabel.setText(String.format("Feels like: %.0f°C \uD83C\uDF21", temperatureFeelsLikeCelsius));

                            if (description.contains("cloud")) {
                                descriptionLabel.setText("Weather Description: " + description + " ☁️"); // Emoji added here
                            } else if (description.contains("rain")) {
                                descriptionLabel.setText("Weather Description: " + description + " \uD83C\uDF27️");
                            } else {
                                descriptionLabel.setText("Weather Description: " + description + " ☀️");
                            }
                            if (!humidityLabel.getText().equals("") && humidityLabel.isVisible()) {
                                humidityLabel.setText(String.format("Humidity: %d%%", humidity));
                                uvLabel.setText("UV Index: " + getUvOutputFormat(getUV(city)));
                                windSpeedLabel.setText(String.format("Wind speed: %.2f km/h", getWindSpeedInKms(weatherData.getWind().getSpeed())));

                                if (!dateForecast.getText().equals("") && dateForecast.isVisible()) {
                                    dateForecast.setText(String.format("Date: %s", forecastData.getDate()));
                                    weatherDescriptionForecast.setText("Weather description for the day: " + forecastData.getWeatherDescription());
                                    maxTempForecast.setText(String.format("Max temperature for the day: %.0f°C", forecastData.getMaxTemp()));
                                    minTempForecast.setText(String.format("Min temperature for the day: %.0f°C", forecastData.getMinTemp()));
                                    avgTempForecast.setText(String.format("Average temperature for the day: %.0f°C", forecastData.getAvgTemp()));
                                    maxWindForecast.setText(String.format("Max wind speed for the day: %.2f km/h", forecastData.getMaxWind()));
                                    avgHumidityForecast.setText("Average humidity for the day: " + forecastData.getAvgHumidity() + "%");
                                    chanceOfRainingForecast.setText(String.format("Chance of raining: %d%%", forecastData.getPercentChanceOfRain()));
                                    chanceOfSnowForecast.setText(String.format("Chance of snowing: %d%%", forecastData.getPercentChanceOfSnow()));
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
                        } else {
                            localTimeLabel.setText("Invalid place.");
                            localTimeLabel.setTextFill(Color.RED);
                            inputTextField.setStyle("-fx-text-fill: red;");
                            showWeeklyForecastButton.setVisible(false);
                            temperatureLabel.setVisible(false);
                            descriptionLabel.setVisible(false);
                            temperatureFeelsLikeLabel.setVisible(false);
                            convertButton.setVisible(false);
                            showMoreButton.setVisible(false);
                            humidityLabel.setText("");
                            humidityLabel.setVisible(false);
                            windSpeedLabel.setVisible(false);
                            convertWindSpeedButton.setVisible(false);
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
                    } catch (Exception e) {
                        e.printStackTrace();
                        localTimeLabel.setText("An error occurred.");
                        localTimeLabel.setTextFill(Color.RED);
                        inputTextField.setStyle("-fx-text-fill: red;");
                        showWeeklyForecastButton.setVisible(false);
                        temperatureLabel.setVisible(false);
                        descriptionLabel.setVisible(false);
                        temperatureFeelsLikeLabel.setVisible(false);
                        convertButton.setVisible(false);
                        showMoreButton.setVisible(false);
                        humidityLabel.setVisible(false);
                        humidityLabel.setText("");
                        windSpeedLabel.setVisible(false);
                        convertWindSpeedButton.setVisible(false);
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
                    if (inputTextField.getText().equals("") && !checkButtonsPane.getChildren().contains(fetchButton)) {
                        inputTextField.setText(cityStartUpTextField.getText());
                        inputTextField.positionCaret(inputTextField.getText().length());
                    }
                });
            }).start();
        }
    }

    private String getUvOutputFormat(double uvIndex) {
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

    private void returnToMainPage() {
        Platform.runLater(() -> {
            stage.setScene(mainScene);
            stage.show();
            temperatureLabel.setVisible(true);
            descriptionLabel.setVisible(true);
            temperatureFeelsLikeLabel.setVisible(true);
            convertTemperature.setVisible(true);
            showMoreWeatherInfo.setVisible(true);
            humidityLabel.setVisible(true);
            windSpeedLabel.setVisible(true);
            convertWindSpeed.setVisible(true);
            uvLabel.setVisible(true);
            getDailyForecast.setVisible(true);
            dateForecast.setVisible(true);
            maxTempForecast.setVisible(true);
            minTempForecast.setVisible(true);
            avgTempForecast.setVisible(true);
            maxWindForecast.setVisible(true);
            avgHumidityForecast.setVisible(true);
            chanceOfRainingForecast.setVisible(true);
            chanceOfSnowForecast.setVisible(true);
            weatherDescriptionForecast.setVisible(true);
            sunrise.setVisible(true);
            sunset.setVisible(true);
            localTimeLabel.setVisible(true);
            inputTextField.setVisible(true);
            fetchButton.setVisible(true);
            cityLabel.setVisible(true);
        });
    }

    private void resetUI() {
        Platform.runLater(() -> {
            temperatureLabel.setVisible(false);
            descriptionLabel.setVisible(false);
            temperatureFeelsLikeLabel.setVisible(false);
            convertTemperature.setVisible(false);
            showMoreWeatherInfo.setVisible(false);
            humidityLabel.setVisible(false);
            windSpeedLabel.setVisible(false);
            convertWindSpeed.setVisible(false);
            uvLabel.setVisible(false);
            getDailyForecast.setVisible(false);
            dateForecast.setVisible(false);
            maxTempForecast.setVisible(false);
            minTempForecast.setVisible(false);
            avgTempForecast.setVisible(false);
            maxWindForecast.setVisible(false);
            avgHumidityForecast.setVisible(false);
            chanceOfRainingForecast.setVisible(false);
            chanceOfSnowForecast.setVisible(false);
            weatherDescriptionForecast.setVisible(false);
            sunrise.setVisible(false);
            sunset.setVisible(false);
            localTimeLabel.setVisible(false);
            inputTextField.setVisible(false);
            fetchButton.setVisible(false);
            cityLabel.setVisible(false);
        });
    }

    private JSONArray getWeeklyForecast() {
        String responseBody;
        try {
            responseBody = ForecastAPI.httpResponseForecast(city);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JSONObject response = new JSONObject(responseBody);
        return response.getJSONObject("forecast").getJSONArray("forecastday");
    }

    private ForecastData getDailyForecast() {
        String responseBody;

        try {
            responseBody = ForecastAPI.httpResponseForecast(city);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (responseBody != null && !responseBody.contains("No matching location found.")) {
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
            double maxWind = day.getDouble("maxwind_kph");
            double avgHumidity = day.getDouble("avghumidity");
            int chanceOfRain = day.getInt("daily_chance_of_rain");
            int chanceOfSnow = day.getInt("daily_chance_of_snow");
            String weatherCondition = weatherConditionObject.getString("text");
            String sunRise = astroObject.getString("sunrise");
            String sunSet = astroObject.getString("sunset");


            return new ForecastData(date, maxTempC, minTempC, avgTempC, maxWind,
                    avgHumidity, chanceOfRain, chanceOfSnow, weatherCondition, sunRise, sunSet);
        }
        return null;
    }

    private double getUV(String city) {

        String responseBody;
        try {
            responseBody = ForecastAPI.httpResponse(city);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(responseBody);
        JSONObject jsonObject = new JSONObject(responseBody);
        JSONObject currentObject = jsonObject.getJSONObject("current");

        double uvValue = currentObject.getDouble("uv");
        System.out.println(uvValue);
        return uvValue;
    }

    private double getTempInCelsius(double temp) {
        return temp - 273.15;
    }

    private double getWindSpeedInKms(double windSpeed) {
        return windSpeed * 3.6;
    }

    private double getTempInFahrenheit(double temp) {
        return (temp - 273.15) * 9 / 5 + 32;
    }

    private double getWindSpeedInMiles(double windSpeed) {
        return windSpeed * 2.23694;
    }

    private void configureShowMoreButtonAction() {
        // Show more weather info logic
        new Thread(() -> {
            // Perform network operations, JSON parsing, and data processing here
            MainParsedData mainInfo = weatherData.getMain();
            double uvIndex = getUV(city);
            Platform.runLater(() -> {
                if (humidityLabel.getText().equals("") && !humidityLabel.isVisible()) {

                    humidityLabel.setText(String.format("Humidity: %d%%", mainInfo.getHumidity()));
                    uvLabel.setText("UV Index: " + getUvOutputFormat(uvIndex));
                    windSpeedLabel.setText(String.format("Wind speed: %.2f km/h", getWindSpeedInKms(weatherData.getWind().getSpeed())));
                    convertWindSpeed.setVisible(true);
                    getDailyForecast.setVisible(true);
                    humidityLabel.setVisible(true);
                    windSpeedLabel.setVisible(true);
                    uvLabel.setVisible(true);

                } else {
                    humidityLabel.setText("");
                    humidityLabel.setVisible(false);
                    windSpeedLabel.setVisible(false);
                    uvLabel.setVisible(false);
                    convertWindSpeed.setVisible(false);
                    getDailyForecast.setVisible(false);
                    showWeeklyForecastButton.setVisible(false);

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
            });
        }).start();
    }

    private String getLocalTime(String city) {
        String responseBody;
        try {
            responseBody = ForecastAPI.httpResponse(city);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (responseBody != null && !responseBody.contains("No matching location found.")) {
            Gson gson = new Gson();
            System.out.println(responseBody);
            ForecastAPIData forecastData = gson.fromJson(responseBody, ForecastAPIData.class);
            return forecastData.getLocation().getLocaltime();
        }
        return null;
    }

    private static String formatDateTime(String inputDateTime) {
        // Date formatting logic
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE");

        try {
            Date date = inputFormat.parse(inputDateTime);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String formatDateToDayAndHour(String inputDateTime) {

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
