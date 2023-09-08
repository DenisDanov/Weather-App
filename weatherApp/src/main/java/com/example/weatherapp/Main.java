package com.example.weatherapp;

import com.example.weatherapp.buttons.ConvertTemperature;
import com.example.weatherapp.buttons.ConvertWindSpeed;
import com.example.weatherapp.buttons.ReturnToFirstPage;
import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import parsingWeatherData.*;
import weatherApi.ForecastAPI;
import weatherApi.WeatherAppAPI;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application {
    private final WeatherAppAPI weatherAppAPI;
    private WeatherData weatherData;
    private String city;
    private final TextField inputTextField = new TextField(); // Create a TextField for input
    private final BubbleLabels temperatureLabel = new BubbleLabels();
    private final BubbleLabels descriptionLabel = new BubbleLabels();
    private final BubbleLabels weatherDescriptionLabel = new BubbleLabels();
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
    private ConvertTemperature convertTemperature;
    private ConvertWindSpeed convertWindSpeed;
    private final Button getDailyForecast = new Button("Show daily forecast");
    private final Button fetchButton = new Button("Show current weather");
    private ReturnToFirstPage returnBackToFirstPage;
    private final Label cityLabel = new Label();
    private final Button showWeeklyForecastButton = new Button("Show weekly forecast");
    private Scene mainScene;
    private StackPane rootLayout = createRootLayout();
    private VBox root;
    private Stage stage;
    private VBox firstPageVbox;
    private final Label cityStartUpLabel = new Label("Enter City or Country:");
    private final Label invalidInput = new Label();
    private final TextField cityStartUpTextField = new TextField();
    private Scene firstPageScene;
    private GridPane buttonsPane;
    private final Pattern pattern = Pattern.compile("[a-zA-Z]");
    private Color originalTextColor;
    private final LinkedHashMap<String, String> responseBodiesFirstAPI;
    private final LinkedHashMap<String, String> responseBodiesSecondAPI;
    private String lastEnteredCity;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private GridPane gridPane = new GridPane();
    private ImageView iconView = new ImageView();
    private String responseBodySecondAPI;
    private String responseBodyCheckForValidInput;
    private String responseBodyGetSunsetSunrise;
    public static String passedFirstPage;
    private String lastWeatherDescription;
    private String lastTimeCheck;
    private final List<Pair<MediaPlayer, Node>> mediaPlayerNodePairs = new ArrayList<>();

    public Main() {
        this.weatherAppAPI = new WeatherAppAPI();
        this.responseBodiesFirstAPI = new LinkedHashMap<>();
        this.responseBodiesSecondAPI = new LinkedHashMap<>();
        this.responseBodyCheckForValidInput = "";
        this.responseBodySecondAPI = "";
        passedFirstPage = "not passed!";
        this.lastEnteredCity = "";
        this.responseBodyGetSunsetSunrise = "";
        this.lastWeatherDescription = "";
        this.lastTimeCheck = "";
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        new Thread(() -> {
            Platform.runLater(() -> {
                mainScene = new Scene(rootLayout, 866, 700);
                stage = primaryStage;
                addStyleSheet(mainScene);
                configurePrimaryStage(primaryStage, mainScene);
                configureStartUpScene();
                setUpDynamicBackground();
                configureFetchButton();
                configureShowMoreButton();
                configureGetDailyForecastButton();
                configureWeeklyForecastButton();
                primaryStage.show();
                updateReturnButtonNodes();
                System.out.println(23);
            });
            Runnable task = () -> {
                // Your code here, what you want to execute every 1 minute
                try {
                    updateAPIData();
                } catch (ParseException | IOException e) {
                    throw new RuntimeException(e);
                }
            };
            // Schedule the task to run every 1 minute, starting immediately
            executorService.scheduleAtFixedRate(task, 0, 5, TimeUnit.MINUTES);
        }).start();
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
    }

    private void setUpDynamicBackground() {
        Media cloudyNightMedia;
        Media cloudyDayMedia;
        Media overcastDayMedia;
        Media overcastNightMedia;
        Media clearNightMedia;
        Media clearDayMedia;
        Media lightRainDayMedia;
        Media lightRainNightMedia;
        Media heavyRainNightMedia;
        Media heavyRainDayMedia;

        heavyRainDayMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-HeavyRain-Day.mp4")).toString());
        MediaPlayer heavyRainDayMediaPlayer = new MediaPlayer(heavyRainDayMedia);
        MediaView heavyRainDayMediaView = new MediaView(heavyRainDayMediaPlayer);

        heavyRainNightMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-HeavyRain-Night.mp4")).toString());
        MediaPlayer heavyRainNightMediaPlayer = new MediaPlayer(heavyRainNightMedia);
        MediaView heavyRainNightMediaView = new MediaView(heavyRainNightMediaPlayer);

        lightRainNightMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-LightRain-Night.mp4")).toString());
        MediaPlayer lightRainNightMediaPlayer = new MediaPlayer(lightRainNightMedia);
        MediaView lightRainNightMediaView = new MediaView(lightRainNightMediaPlayer);

        lightRainDayMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-LightRain-Day.mp4")).toString());
        MediaPlayer lightRainDayMediaPlayer = new MediaPlayer(lightRainDayMedia);
        MediaView lightRainDayMediaView = new MediaView(lightRainDayMediaPlayer);

        cloudyNightMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-Cloudy-Night.mp4")).toString());
        MediaPlayer cloudyNightPlayer = new MediaPlayer(cloudyNightMedia);
        cloudyNightPlayer.setMute(true);
        MediaView cloudyNightMediaView = new MediaView(cloudyNightPlayer);

        cloudyDayMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-Cloudy-Day.mp4")).toString());
        MediaPlayer cloudyDayMediaPlayer = new MediaPlayer(cloudyDayMedia);
        MediaView cloudyDayMediaView = new MediaView(cloudyDayMediaPlayer);

        overcastDayMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-Overcast-Day.mp4")).toString());
        MediaPlayer overcastDayPlayer = new MediaPlayer(overcastDayMedia);
        overcastDayPlayer.setMute(true);
        MediaView overcastDayMediaView = new MediaView(overcastDayPlayer);

        overcastNightMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-Overcast-Night.mp4")).toString());
        MediaPlayer overcastNightPlayer = new MediaPlayer(overcastNightMedia);
        MediaView overcastNightMediaView = new MediaView(overcastNightPlayer);

        clearNightMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-Clear-Night.mp4")).toString());
        MediaPlayer clearNightPlayer = new MediaPlayer(clearNightMedia);
        MediaView clearNightMediaView = new MediaView(clearNightPlayer);

        clearDayMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-Clear-Day.mp4")).toString());
        MediaPlayer clearDayPlayer = new MediaPlayer(clearDayMedia);
        MediaView clearDayMediaView = new MediaView(clearDayPlayer);

        Objects.requireNonNull(rootLayout).getChildren().addAll(//
                lightRainDayMediaView, //
                cloudyNightMediaView, //
                overcastDayMediaView, //
                clearNightMediaView, //
                clearDayMediaView, //
                lightRainNightMediaView, //
                heavyRainDayMediaView, //
                heavyRainNightMediaView, //
                cloudyDayMediaView, //
                overcastNightMediaView, //
                root //
        );
        mediaPlayerNodePairs.add(new Pair<>(lightRainDayMediaPlayer, rootLayout.getChildren().get(0)));
        mediaPlayerNodePairs.add(new Pair<>(cloudyNightPlayer, rootLayout.getChildren().get(1)));
        mediaPlayerNodePairs.add(new Pair<>(overcastDayPlayer, rootLayout.getChildren().get(2)));
        mediaPlayerNodePairs.add(new Pair<>(clearNightPlayer, rootLayout.getChildren().get(3)));
        mediaPlayerNodePairs.add(new Pair<>(clearDayPlayer, rootLayout.getChildren().get(4)));
        mediaPlayerNodePairs.add(new Pair<>(lightRainNightMediaPlayer, rootLayout.getChildren().get(5)));
        mediaPlayerNodePairs.add(new Pair<>(heavyRainDayMediaPlayer, rootLayout.getChildren().get(6)));
        mediaPlayerNodePairs.add(new Pair<>(heavyRainNightMediaPlayer, rootLayout.getChildren().get(7)));
        mediaPlayerNodePairs.add(new Pair<>(cloudyDayMediaPlayer, rootLayout.getChildren().get(8)));
        mediaPlayerNodePairs.add(new Pair<>(overcastNightPlayer, rootLayout.getChildren().get(9)));
    }

    private StackPane createRootLayout() {
        new Thread(() -> Platform.runLater(() -> {
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
            returnBackToFirstPage = new ReturnToFirstPage(stage,
                    firstPageScene,
                    invalidInput,
                    firstPageVbox,
                    fetchButton,
                    cityStartUpTextField,
                    inputTextField,
                    temperatureLabel);
            returnBackToFirstPage.setText("Return to the first page");
            buttonsPane.add(returnBackToFirstPage, 1, 0);
            buttonsPane.setHgap(5);

            // Add the TableView to the root layout
            Objects.requireNonNull(root).getChildren().addAll(cityLabel, inputTextField,
                    buttonsPane,
                    localTimeLabel,
                    temperatureLabel,
                    temperatureFeelsLikeLabel,
                    descriptionLabel);

            convertTemperature = new ConvertTemperature(temperatureLabel, temperatureFeelsLikeLabel);
            convertTemperature.setText("Convert temperature");
            root.getChildren().add(6, convertTemperature);

            convertWindSpeed = new ConvertWindSpeed(windSpeedLabel);
            convertWindSpeed.setText("Convert wind speed");

            root.getChildren().addAll(showMoreWeatherInfo, humidityLabel, uvLabel,
                    windSpeedLabel, convertWindSpeed, getDailyForecast);
            root.getChildren().addAll(dateForecast, maxTempForecast, minTempForecast, avgTempForecast,
                    maxWindForecast, avgHumidityForecast, chanceOfRainingForecast,
                    chanceOfSnowForecast, weatherDescriptionForecast, sunrise, sunset, showWeeklyForecastButton);

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
        })).start();
        return rootLayout;
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

    private void stopAllMediaPlayersAndHideAllNodes() {
        for (Pair<MediaPlayer, Node> pair : mediaPlayerNodePairs) {
            pair.getKey().stop();
            pair.getValue().setVisible(false);
        }
    }

    private void switchVideoBackground(String weatherDescription) {
        boolean currentTimeIsLaterThanSunsetVar = currentTimeIsLaterThanSunset();
        Platform.runLater(() -> {
            if (!lastWeatherDescription.equals(weatherDescription)) {
                stopAllMediaPlayersAndHideAllNodes();
            } else {
                if (!currentTimeIsLaterThanSunsetVar && lastTimeCheck.equals("Day")) {
                    stopAllMediaPlayersAndHideAllNodes();
                } else if (currentTimeIsLaterThanSunsetVar && lastTimeCheck.equals("Night")) {
                    stopAllMediaPlayersAndHideAllNodes();
                }
            }
            if (weatherDescription.toLowerCase().contains("light rain") &&
                    currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(0).getKey().play();
                mediaPlayerNodePairs.get(0).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(0).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("cloud") &&
                    !currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(1).getKey().play();
                mediaPlayerNodePairs.get(1).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(1).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("overcast") &&
                    currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(2).getKey().play();
                mediaPlayerNodePairs.get(2).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(2).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("clear") &&
                    !currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(3).getKey().play();
                mediaPlayerNodePairs.get(3).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(3).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("clear") &&
                    currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(4).getKey().play();
                mediaPlayerNodePairs.get(4).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(4).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("light rain") &&
                    !currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(5).getKey().play();
                mediaPlayerNodePairs.get(5).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(5).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("heavy rain") &&
                    currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(6).getKey().play();
                mediaPlayerNodePairs.get(6).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(6).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("heavy rain") &&
                    !currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(7).getKey().play();
                mediaPlayerNodePairs.get(7).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(7).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("sunny") && !currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(3).getKey().play();
                mediaPlayerNodePairs.get(3).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(3).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("sunny") && currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(4).getKey().play();
                mediaPlayerNodePairs.get(4).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(4).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("rain") &&
                    currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(6).getKey().play();
                mediaPlayerNodePairs.get(6).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(6).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("rain") &&
                    !currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(7).getKey().play();
                mediaPlayerNodePairs.get(7).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(7).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("overcast") &&
                    !currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(9).getKey().play();
                mediaPlayerNodePairs.get(9).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(9).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("cloud") &&
                    currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(8).getKey().play();
                mediaPlayerNodePairs.get(8).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(8).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("mist") &&
                    currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(8).getKey().play();
                mediaPlayerNodePairs.get(8).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(8).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("fog") &&
                    currentTimeIsLaterThanSunsetVar) {
                mediaPlayerNodePairs.get(8).getKey().play();
                mediaPlayerNodePairs.get(8).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(8).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("mist") &&
                    !currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(1).getKey().play();
                mediaPlayerNodePairs.get(1).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(1).getValue().setVisible(true);
            } else if (weatherDescription.toLowerCase().contains("fog") &&
                    !currentTimeIsLaterThanSunsetVar) {

                mediaPlayerNodePairs.get(1).getKey().play();
                mediaPlayerNodePairs.get(1).getKey().setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerNodePairs.get(1).getValue().setVisible(true);
            }
            if (!lastWeatherDescription.equals(weatherDescription)) {
                lastWeatherDescription = weatherDescription;
            }
            if (!currentTimeIsLaterThanSunsetVar) {
                lastTimeCheck = "Night";
            } else {
                lastTimeCheck = "Day";
            }
        });
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
                responseBodySecondAPI = getLocalTime(city);
            }
            Platform.runLater(() -> {
                if (responseBodyCheckForValidInput.equals("{\"cod\":\"400\",\"message\":\"Nothing to geocode\"}") ||
                        responseBodyCheckForValidInput.equals("{\"cod\":\"404\",\"message\":\"city not found\"}") || !matcher.find()
                        || responseBodySecondAPI == null || responseBodySecondAPI.contains("No matching location found.")) {
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
            JSONArray weeklyForecast = getWeeklyForecast();
            JSONObject[] daysOfTheWeek = new JSONObject[7];
            for (int i = 0; i < weeklyForecast.length(); i++) {
                daysOfTheWeek[i] = weeklyForecast.getJSONObject(i);
            }
            // Add day columns
            JSONObject day1 = daysOfTheWeek[0];
            JSONObject day2 = daysOfTheWeek[1];
            JSONObject day3 = daysOfTheWeek[2];
            JSONObject day4 = daysOfTheWeek[3];
            JSONObject day5 = daysOfTheWeek[4];
            JSONObject day6 = daysOfTheWeek[5];
            JSONObject day7 = daysOfTheWeek[6];
            TableView<WeeklyForecastTable> table = new TableView<>();

            // Create a "Data Type" column
            TableColumn<WeeklyForecastTable, String> dataTypeColumn = new TableColumn<>("Day");
            dataTypeColumn.setCellValueFactory(data -> {

                int rowIndex = data.getTableView().getItems().indexOf(data.getValue());
                if (rowIndex == 0) {
                    return new SimpleStringProperty("Max Temp");
                } else if (rowIndex == 1) {
                    return new SimpleStringProperty("Min Temp");
                } else if (rowIndex == 2) {
                    return new SimpleStringProperty("Avg Temp");
                } else if (rowIndex == 3) {
                    return new SimpleStringProperty("Max Wind");
                } else if (rowIndex == 4) {
                    return new SimpleStringProperty("Avg Humidity");
                } else if (rowIndex == 5) {
                    return new SimpleStringProperty("UV Index");
                } else if (rowIndex == 6) {
                    return new SimpleStringProperty("Chance of Rain");
                } else if (rowIndex == 7) {
                    return new SimpleStringProperty("Chance of Snow");
                } else if (rowIndex == 8) {
                    return new SimpleStringProperty("Weather Description");
                } else if (rowIndex == 9) {
                    return new SimpleStringProperty("Sunrise");
                } else if (rowIndex == 10) {
                    return new SimpleStringProperty("Sunset");
                } else {
                    return new SimpleStringProperty("");
                }

            });
            dataTypeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            dataTypeColumn.setEditable(true);

            // Create columns for each day of the week (including current day)
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE");
            for (int i = 0; i < 7; i++) {
                final int index = i;
                TableColumn<WeeklyForecastTable, String> column = new TableColumn<>(currentDate.format(dateFormatter));
                column.setResizable(true);
                column.setCellValueFactory(data -> {
                    int rowIndex = data.getTableView().getItems().indexOf(data.getValue());
                    return new SimpleStringProperty(data.getValue().getData(rowIndex, index));
                });
                column.setCellFactory(TextFieldTableCell.forTableColumn());
                column.setOnEditCommit(event -> {
                    WeeklyForecastTable weeklyForecastTable = event.getRowValue();
                    weeklyForecastTable.setData(event.getTablePosition().getRow(), index, event.getNewValue());
                });
                column.setCellFactory(column1 -> new TableCell<>() {
                    private final Text text;

                    {
                        text = new Text();
                        text.wrappingWidthProperty().bind(column1.widthProperty().subtract(4));
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
                table.getColumns().add(column);
                currentDate = currentDate.plusDays(1);
            }

            // Add columns to the table
            table.getColumns().add(0, dataTypeColumn);

            // Create and add sample data
            ObservableList<WeeklyForecastTable> data = FXCollections.observableArrayList();
            for (int i = 0; i < 11; i++) {
                data.add(new WeeklyForecastTable());
            }
            // Add sample temperature data for the second row (Min Temp) and third row (Avg Temp)
            data.get(0).setData(0, 0, String.format("%.0f°C", (day1.getJSONObject("day").getDouble("maxtemp_c"))));
            data.get(0).setData(0, 1, String.format("%.0f°C", (day2.getJSONObject("day").getDouble("maxtemp_c"))));
            data.get(0).setData(0, 2, String.format("%.0f°C", (day3.getJSONObject("day").getDouble("maxtemp_c"))));
            data.get(0).setData(0, 3, String.format("%.0f°C", (day4.getJSONObject("day").getDouble("maxtemp_c"))));
            data.get(0).setData(0, 4, String.format("%.0f°C", (day5.getJSONObject("day").getDouble("maxtemp_c"))));
            data.get(0).setData(0, 5, String.format("%.0f°C", (day6.getJSONObject("day").getDouble("maxtemp_c"))));
            data.get(0).setData(0, 6, String.format("%.0f°C", (day7.getJSONObject("day").getDouble("maxtemp_c"))));

            data.get(1).setData(1, 0, String.format("%.0f°C", (day1.getJSONObject("day").getDouble("mintemp_c"))));
            data.get(1).setData(1, 1, String.format("%.0f°C", (day2.getJSONObject("day").getDouble("mintemp_c"))));
            data.get(1).setData(1, 2, String.format("%.0f°C", (day3.getJSONObject("day").getDouble("mintemp_c"))));
            data.get(1).setData(1, 3, String.format("%.0f°C", (day4.getJSONObject("day").getDouble("mintemp_c"))));
            data.get(1).setData(1, 4, String.format("%.0f°C", (day5.getJSONObject("day").getDouble("mintemp_c"))));
            data.get(1).setData(1, 5, String.format("%.0f°C", (day6.getJSONObject("day").getDouble("mintemp_c"))));
            data.get(1).setData(1, 6, String.format("%.0f°C", (day7.getJSONObject("day").getDouble("mintemp_c"))));

            data.get(2).setData(2, 0, String.format("%.0f°C", (day1.getJSONObject("day").getDouble("avgtemp_c"))));
            data.get(2).setData(2, 1, String.format("%.0f°C", (day2.getJSONObject("day").getDouble("avgtemp_c"))));
            data.get(2).setData(2, 2, String.format("%.0f°C", (day3.getJSONObject("day").getDouble("avgtemp_c"))));
            data.get(2).setData(2, 3, String.format("%.0f°C", (day4.getJSONObject("day").getDouble("avgtemp_c"))));
            data.get(2).setData(2, 4, String.format("%.0f°C", (day5.getJSONObject("day").getDouble("avgtemp_c"))));
            data.get(2).setData(2, 5, String.format("%.0f°C", (day6.getJSONObject("day").getDouble("avgtemp_c"))));
            data.get(2).setData(2, 6, String.format("%.0f°C", (day7.getJSONObject("day").getDouble("avgtemp_c"))));

            data.get(3).setData(3, 0, String.format("%.0f km/h", (day1.getJSONObject("day").getDouble("maxwind_kph"))));
            data.get(3).setData(3, 1, String.format("%.0f km/h", (day2.getJSONObject("day").getDouble("maxwind_kph"))));
            data.get(3).setData(3, 2, String.format("%.0f km/h", (day3.getJSONObject("day").getDouble("maxwind_kph"))));
            data.get(3).setData(3, 3, String.format("%.0f km/h", (day4.getJSONObject("day").getDouble("maxwind_kph"))));
            data.get(3).setData(3, 4, String.format("%.0f km/h", (day5.getJSONObject("day").getDouble("maxwind_kph"))));
            data.get(3).setData(3, 5, String.format("%.0f km/h", (day6.getJSONObject("day").getDouble("maxwind_kph"))));
            data.get(3).setData(3, 6, String.format("%.0f km/h", (day7.getJSONObject("day").getDouble("maxwind_kph"))));

            data.get(4).setData(4, 0, String.format("%.0f %%", (day1.getJSONObject("day").getDouble("avghumidity"))));
            data.get(4).setData(4, 1, String.format("%.0f %%", (day2.getJSONObject("day").getDouble("avghumidity"))));
            data.get(4).setData(4, 2, String.format("%.0f %%", (day3.getJSONObject("day").getDouble("avghumidity"))));
            data.get(4).setData(4, 3, String.format("%.0f %%", (day4.getJSONObject("day").getDouble("avghumidity"))));
            data.get(4).setData(4, 4, String.format("%.0f %%", (day5.getJSONObject("day").getDouble("avghumidity"))));
            data.get(4).setData(4, 5, String.format("%.0f %%", (day6.getJSONObject("day").getDouble("avghumidity"))));
            data.get(4).setData(4, 6, String.format("%.0f %%", (day7.getJSONObject("day").getDouble("avghumidity"))));

            data.get(5).setData(5, 0, getUvOutputFormat(day1.getJSONObject("day").getDouble("uv")));
            data.get(5).setData(5, 1, getUvOutputFormat(day2.getJSONObject("day").getDouble("uv")));
            data.get(5).setData(5, 2, getUvOutputFormat(day3.getJSONObject("day").getDouble("uv")));
            data.get(5).setData(5, 3, getUvOutputFormat(day4.getJSONObject("day").getDouble("uv")));
            data.get(5).setData(5, 4, getUvOutputFormat(day5.getJSONObject("day").getDouble("uv")));
            data.get(5).setData(5, 5, getUvOutputFormat(day6.getJSONObject("day").getDouble("uv")));
            data.get(5).setData(5, 6, getUvOutputFormat(day7.getJSONObject("day").getDouble("uv")));

            data.get(6).setData(6, 0, String.format("%.0f %%", (day1.getJSONObject("day").getDouble("daily_chance_of_rain"))));
            data.get(6).setData(6, 1, String.format("%.0f %%", (day2.getJSONObject("day").getDouble("daily_chance_of_rain"))));
            data.get(6).setData(6, 2, String.format("%.0f %%", (day3.getJSONObject("day").getDouble("daily_chance_of_rain"))));
            data.get(6).setData(6, 3, String.format("%.0f %%", (day4.getJSONObject("day").getDouble("daily_chance_of_rain"))));
            data.get(6).setData(6, 4, String.format("%.0f %%", (day5.getJSONObject("day").getDouble("daily_chance_of_rain"))));
            data.get(6).setData(6, 5, String.format("%.0f %%", (day6.getJSONObject("day").getDouble("daily_chance_of_rain"))));
            data.get(6).setData(6, 6, String.format("%.0f %%", (day7.getJSONObject("day").getDouble("daily_chance_of_rain"))));

            data.get(7).setData(7, 0, String.format("%.0f %%", (day1.getJSONObject("day").getDouble("daily_chance_of_snow"))));
            data.get(7).setData(7, 1, String.format("%.0f %%", (day2.getJSONObject("day").getDouble("daily_chance_of_snow"))));
            data.get(7).setData(7, 2, String.format("%.0f %%", (day3.getJSONObject("day").getDouble("daily_chance_of_snow"))));
            data.get(7).setData(7, 3, String.format("%.0f %%", (day4.getJSONObject("day").getDouble("daily_chance_of_snow"))));
            data.get(7).setData(7, 4, String.format("%.0f %%", (day5.getJSONObject("day").getDouble("daily_chance_of_snow"))));
            data.get(7).setData(7, 5, String.format("%.0f %%", (day6.getJSONObject("day").getDouble("daily_chance_of_snow"))));
            data.get(7).setData(7, 6, String.format("%.0f %%", (day7.getJSONObject("day").getDouble("daily_chance_of_snow"))));

            data.get(8).setData(8, 0, (day1.getJSONObject("day").getJSONObject("condition").getString("text")));
            data.get(8).setData(8, 1, (day2.getJSONObject("day").getJSONObject("condition").getString("text")));
            data.get(8).setData(8, 2, (day3.getJSONObject("day").getJSONObject("condition").getString("text")));
            data.get(8).setData(8, 3, (day4.getJSONObject("day").getJSONObject("condition").getString("text")));
            data.get(8).setData(8, 4, (day5.getJSONObject("day").getJSONObject("condition").getString("text")));
            data.get(8).setData(8, 5, (day6.getJSONObject("day").getJSONObject("condition").getString("text")));
            data.get(8).setData(8, 6, (day7.getJSONObject("day").getJSONObject("condition").getString("text")));

            data.get(9).setData(9, 0, (day1.getJSONObject("astro").getString("sunrise")));
            data.get(9).setData(9, 1, (day2.getJSONObject("astro").getString("sunrise")));
            data.get(9).setData(9, 2, (day3.getJSONObject("astro").getString("sunrise")));
            data.get(9).setData(9, 3, (day4.getJSONObject("astro").getString("sunrise")));
            data.get(9).setData(9, 4, (day5.getJSONObject("astro").getString("sunrise")));
            data.get(9).setData(9, 5, (day6.getJSONObject("astro").getString("sunrise")));
            data.get(9).setData(9, 6, (day7.getJSONObject("astro").getString("sunrise")));

            data.get(10).setData(10, 0, (day1.getJSONObject("astro").getString("sunset")));
            data.get(10).setData(10, 1, (day2.getJSONObject("astro").getString("sunset")));
            data.get(10).setData(10, 2, (day3.getJSONObject("astro").getString("sunset")));
            data.get(10).setData(10, 3, (day4.getJSONObject("astro").getString("sunset")));
            data.get(10).setData(10, 4, (day5.getJSONObject("astro").getString("sunset")));
            data.get(10).setData(10, 5, (day6.getJSONObject("astro").getString("sunset")));
            data.get(10).setData(10, 6, (day7.getJSONObject("astro").getString("sunset")));

            table.setItems(data);

            dataTypeColumn.setCellFactory(column -> new TableCell<>() {
                private final Text text;

                {
                    text = new Text();
                    text.wrappingWidthProperty().bind(dataTypeColumn.widthProperty().subtract(4.8));
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

            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            double tableViewWidth = 0;
            for (TableColumn<?, ?> column : table.getColumns()) {
                tableViewWidth += column.getWidth();
            }
            table.setPrefHeight(430);
            VBox root = new VBox(table);
            Scene scene = new Scene(root, tableViewWidth, 650);

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
                        dateForecast.setText(String.format("Date: %s", Objects.requireNonNull(forecastData).getDate()));
                        weatherDescriptionForecast.setText("Weather description for the day: " + forecastData.getWeatherDescription());
                        maxTempForecast.setText(String.format("Max temperature for the day: %.0f°C", forecastData.getMaxTemp()));
                        minTempForecast.setText(String.format("Min temperature for the day: %.0f°C", forecastData.getMinTemp()));
                        avgTempForecast.setText(String.format("Average temperature for the day: %.0f°C", forecastData.getAvgTemp()));
                        maxWindForecast.setText(String.format("Max wind speed for the day: %.0f km/h", forecastData.getMaxWind()));
                        avgHumidityForecast.setText(String.format("Average humidity for the day: %.0f %%", forecastData.getAvgHumidity()));
                        chanceOfRainingForecast.setText(String.format("Chance of raining: %d %%", forecastData.getPercentChanceOfRain()));
                        chanceOfSnowForecast.setText(String.format("Chance of snowing: %d %%", forecastData.getPercentChanceOfSnow()));
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

    private void fetchAndDisplayWeatherData(String cityTextField) throws IOException, ParseException {
        // Fetch and display weather data logic
        this.city = cityTextField;
        if (stage.getScene() == firstPageScene && !passedFirstPage.equals("Passed!")) {
            try {
                checkForValidInput();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            // Perform network operations, JSON parsing, and data processing here
            new Thread(() -> {
                String weatherConditionAndIcon = getWeatherCondition();
                if (!city.equals(lastEnteredCity) && !weatherConditionAndIcon.equals("")) {
                    switchVideoBackground(weatherConditionAndIcon.split("&")[1]);
                }
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
                convertTemperature.setWeatherData(weatherData);
                convertWindSpeed.setWeatherData(weatherData);
                Platform.runLater(() -> {
                    if (stage.getScene() != mainScene) {
                        stage.setScene(mainScene);
                    }
                    GridPane checkButtonsPane = (GridPane) root.getChildren().get(2);
                    if (!checkButtonsPane.getChildren().contains(fetchButton)) {
                        buttonsPane.add(fetchButton, 0, 0);
                    }
                    if (localTimeLabel.getTextFill().equals(Color.RED)) {
                        localTimeLabel.setTextFill(originalTextColor);
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
                        if (mainInfo != null && weatherInfo != null && weatherInfo.length > 0 && getLocalTime(city) != null && forecastData != null) {
                            if (!lastEnteredCity.equals(city)) {
                                if (inputTextField.getStyle().equals("-fx-text-fill: red;")) {
                                    inputTextField.setStyle(temperatureLabel.getStyle());
                                }
                                if (!responseBodiesFirstAPI.containsKey(city)) {
                                    responseBodiesFirstAPI.put(city, responseBody);
                                }
                                double temp = mainInfo.getTemp();
                                double tempFeelsLike = mainInfo.getFeels_like();
                                int humidity = mainInfo.getHumidity();
                                // Update your labels here
                                buttonsPane.setVisible(true);
                                convertTemperature.setVisible(true);
                                showMoreWeatherInfo.setVisible(true);
                                temperatureLabel.setVisible(true);
                                descriptionLabel.setVisible(true);
                                temperatureFeelsLikeLabel.setVisible(true);

                                double temperatureCelsius = (temp - 273.15);
                                double temperatureFeelsLikeCelsius = tempFeelsLike - 273.15;
                                localTimeLabel.setText(String.format("Local time: %s", formatDateToDayAndHour(getLocalTime(city))));
                                originalTextColor = (Color) localTimeLabel.getTextFill();
                                temperatureLabel.setText(String.format("Temperature: %.0f°C \uD83C\uDF21", temperatureCelsius));
                                temperatureFeelsLikeLabel.setText(String.format("Feels like: %.0f°C \uD83C\uDF21", temperatureFeelsLikeCelsius));

                                weatherDescriptionLabel.setWrapText(true);
                                weatherDescriptionLabel.setText("Weather Description: " + weatherConditionAndIcon.split("&")[1]);
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
                                    windSpeedLabel.setText(String.format("Wind speed: %.0f km/h", (weatherData.getWind().getSpeed() * 3.6)));

                                    if (!dateForecast.getText().equals("") && dateForecast.isVisible()) {
                                        dateForecast.setText(String.format("Date: %s", forecastData.getDate()));
                                        weatherDescriptionForecast.setText("Weather description for the day: " + forecastData.getWeatherDescription());
                                        maxTempForecast.setText(String.format("Max temperature for the day: %.0f°C", forecastData.getMaxTemp()));
                                        minTempForecast.setText(String.format("Min temperature for the day: %.0f°C", forecastData.getMinTemp()));
                                        avgTempForecast.setText(String.format("Average temperature for the day: %.0f°C", forecastData.getAvgTemp()));
                                        maxWindForecast.setText(String.format("Max wind speed for the day: %.0f km/h", forecastData.getMaxWind()));
                                        avgHumidityForecast.setText(String.format("Average humidity for the day: %.0f %%", forecastData.getAvgHumidity()));
                                        chanceOfRainingForecast.setText(String.format("Chance of raining: %d %%", forecastData.getPercentChanceOfRain()));
                                        chanceOfSnowForecast.setText(String.format("Chance of snowing: %d %%", forecastData.getPercentChanceOfSnow()));
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
                            localTimeLabel.setTextFill(Color.RED);
                            inputTextField.setStyle("-fx-text-fill: red;");
                            showWeeklyForecastButton.setVisible(false);
                            temperatureLabel.setVisible(false);
                            descriptionLabel.setVisible(false);
                            temperatureFeelsLikeLabel.setVisible(false);
                            convertTemperature.setVisible(false);
                            showMoreWeatherInfo.setVisible(false);
                            humidityLabel.setText("");
                            humidityLabel.setVisible(false);
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
                    } catch (Exception e) {
                        e.printStackTrace();
                        localTimeLabel.setText("An error occurred.");
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
                    if (inputTextField.getText().equals("")) {
                        inputTextField.setText(cityStartUpTextField.getText());
                        inputTextField.deselect();
                        Platform.runLater(() -> inputTextField.positionCaret(cityStartUpTextField.getText().length()));
                    }
                });
            }).start();
        }
    }

    private boolean currentTimeIsLaterThanSunset() {
        String currentTimeTrimmed = formatDateToDayAndHour(getLocalTime(city)).split(", ")[1];

        List<String> sunsetAndSunrise = getSunsetAndSunrise();
        String sunsetTimeTrimmed = Objects.requireNonNull(sunsetAndSunrise).get(0);
        String sunriseTimeTrimmed = Objects.requireNonNull(sunsetAndSunrise).get(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

        // Parse the strings into LocalTime objects
        // Compare the two LocalTime objects
        LocalTime currentTime = LocalTime.parse(currentTimeTrimmed, formatter);
        LocalTime sunriseTime = LocalTime.parse(sunriseTimeTrimmed, formatter);
        LocalTime sunsetTime = LocalTime.parse(sunsetTimeTrimmed, formatter);

        // Check if the current time is between sunrise and sunset
        if (currentTime.isAfter(sunriseTime) && currentTime.isBefore(sunsetTime)) {
            return true; // It's daytime
        } else {
            return false; // It's nighttime
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
        JSONObject response = new JSONObject(responseBody);
        return response.getJSONObject("forecast").getJSONArray("forecastday");
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
            System.out.printf("Updated %d APIs!\n", responseBodiesSecondAPI.size());
            for (String cityKey : responseBodiesFirstAPI.keySet()) {
                try {
                    responseBodiesFirstAPI.replace(cityKey, weatherAppAPI.httpResponse(cityKey));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.printf("Updated %d APIs!\n", responseBodiesFirstAPI.size());
        }).start();
    }

    private ForecastData getDailyForecast() {
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
        if (responseBody != null && !responseBody.contains("No matching location found.")) {
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

    private List<String> getSunsetAndSunrise() {
        List<String> sunsetAndSunrise = new CopyOnWriteArrayList<>();
        Thread thread = new Thread(() -> {
            if (!responseBodiesSecondAPI.containsKey(city)) {
                try {
                    responseBodyGetSunsetSunrise = ForecastAPI.httpResponseForecast(city);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                responseBodyGetSunsetSunrise = responseBodiesSecondAPI.get(city);
            }
            if (responseBodyGetSunsetSunrise != null && !responseBodyGetSunsetSunrise.contains("No matching location found.")) {
                if (!responseBodiesSecondAPI.containsKey(city)) {
                    responseBodiesSecondAPI.put(city, responseBodyGetSunsetSunrise);
                }
                JSONObject response = new JSONObject(responseBodyGetSunsetSunrise);

                JSONArray forecastDays = response.getJSONObject("forecast").getJSONArray("forecastday");

                JSONObject forecast = forecastDays.getJSONObject(0);

                JSONObject astroObject = forecast.getJSONObject("astro");

                sunsetAndSunrise.add(astroObject.getString("sunset"));
                sunsetAndSunrise.add(astroObject.getString("sunrise"));
            }
        });
        thread.start();
        try {
            thread.join(); // Wait for the thread to finish
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return sunsetAndSunrise;
    }

    private double getUV(String city) {
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
        if (responseBody != null && !responseBody.contains("No matching location found.")) {
            if (!responseBodiesSecondAPI.containsKey(city)) {
                responseBodiesSecondAPI.put(city, responseBody);
            }
            JSONObject response = new JSONObject(responseBody);
            JSONObject weatherCondition = response.getJSONObject("current").getJSONObject("condition");
            weatherConditionAndIcon = (weatherCondition.getString("icon") + "&" + weatherCondition.get("text"));
        }
        return weatherConditionAndIcon;
    }

    private void configureShowMoreButtonAction() {
        // Show more weather info logic
        new Thread(() -> {
            // Perform network operations, JSON parsing, and data processing here
            MainParsedData mainInfo = weatherData.getMain();
            double uvIndex = getUV(city);
            Platform.runLater(() -> {
                if (humidityLabel.getText().equals("") && !humidityLabel.isVisible()) {

                    humidityLabel.setText(String.format("Humidity: %d %%", mainInfo.getHumidity()));
                    uvLabel.setText("UV Index: " + getUvOutputFormat(uvIndex));
                    windSpeedLabel.setText(String.format("Wind speed: %.0f km/h", (weatherData.getWind().getSpeed() * 3.6)));
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
        if (!responseBodiesSecondAPI.containsKey(city)) {
            try {
                responseBody = ForecastAPI.httpResponseForecast(city);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            responseBody = responseBodiesSecondAPI.get(city);
        }
        if (responseBody != null && !responseBody.contains("No matching location found.")) {
            Gson gson = new Gson();
            ForecastAPIData forecastData = gson.fromJson(responseBody, ForecastAPIData.class);
            return forecastData.getLocation().getLocaltime();
        }
        return null;
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