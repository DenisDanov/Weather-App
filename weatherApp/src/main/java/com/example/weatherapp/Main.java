package com.example.weatherapp;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
    private MediaView rainViewMedia = new MediaView();
    private MediaPlayer rainMediaPlayer;
    private MediaView cloudyNightMediaView = new MediaView();
    private MediaPlayer cloudyNightPlayer;
    private MediaView overcastDayMediaView = new MediaView();
    private MediaPlayer overcastDayPlayer;
    private String responseBodySecondAPI;
    private String responseBody;
    private String passedFirstPage;

    public Main() {
        this.weatherAppAPI = new WeatherAppAPI();
        this.responseBodiesFirstAPI = new LinkedHashMap<>();
        this.responseBodiesSecondAPI = new LinkedHashMap<>();
        this.responseBody = "";
        this.responseBodySecondAPI = "";
        this.passedFirstPage = "not passed!";
        this.lastEnteredCity = "";
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
                configureFetchButton();
                configureGoBackToFirstPageButton();
                configureConvertTemperatureButton();
                configureShowMoreButton();
                configureConvertWindSpeedButton();
                configureGetDailyForecastButton();
                configureWeeklyForecastButton();
                primaryStage.show();
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
            executorService.scheduleAtFixedRate(task, 0, 1, TimeUnit.MINUTES);
        }).start();
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
            buttonsPane.add(goBackToFirstPage, 1, 0);
            buttonsPane.setHgap(5);

            // Add the TableView to the root layout
            Objects.requireNonNull(root).getChildren().addAll(cityLabel, inputTextField,
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

            Media raindMedia;
            Media cloudyNightMedia;
            Media overcastDayMedia;

            raindMedia = new Media(Objects.requireNonNull(getClass().getResource("/screen-recorder-08-28-2023-12-54-13-642_tGY0iQ7a (online-video-cutter.com) (2).mp4")).toString());
            rainMediaPlayer = new MediaPlayer(raindMedia);
            rainViewMedia = new MediaView(rainMediaPlayer);

            cloudyNightMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-Cloudy-Night.mp4")).toString());
            cloudyNightPlayer = new MediaPlayer(cloudyNightMedia);
            cloudyNightPlayer.setMute(true);
            cloudyNightMediaView = new MediaView(cloudyNightPlayer);

            overcastDayMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-Overcast-Day.mp4")).toString());
            overcastDayPlayer = new MediaPlayer(overcastDayMedia);
            overcastDayPlayer.setMute(true);
            overcastDayMediaView = new MediaView(overcastDayPlayer);

            rainViewMedia.setVisible(false);
            overcastDayMediaView.setVisible(false);
            cloudyNightMediaView.setVisible(false);

            Objects.requireNonNull(rootLayout).getChildren().addAll(rainViewMedia, this.cloudyNightMediaView, overcastDayMediaView, root);

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

    private void switchVideoBackground(String weatherDescription) {
        new Thread(() -> Platform.runLater(() -> {
            if (weatherDescription.contains("rain") || weatherDescription.contains("Rain")) {
                cloudyNightPlayer.stop();
                rootLayout.getChildren().get(1).setVisible(false);

                rainMediaPlayer.play();
                rainMediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                rootLayout.getChildren().get(0).setVisible(true);
            } else if (weatherDescription.contains("cloud") || weatherDescription.contains("Cloud")){
                rainMediaPlayer.stop();
                rootLayout.getChildren().get(0).setVisible(false);

                overcastDayPlayer.stop();
                rootLayout.getChildren().get(2).setVisible(false);

                cloudyNightPlayer.play();
                cloudyNightPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                rootLayout.getChildren().get(1).setVisible(true);
            } else if (weatherDescription.contains("Overcast")){
                rainMediaPlayer.stop();
                rootLayout.getChildren().get(0).setVisible(false);

                cloudyNightPlayer.stop();
                rootLayout.getChildren().get(1).setVisible(false);

                overcastDayPlayer.play();
                overcastDayPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                rootLayout.getChildren().get(2).setVisible(true);

            }
        })).start();
    }

    private void checkForValidInput() throws IOException {

        Matcher matcher = pattern.matcher(city);
        new Thread(() -> {
            if (matcher.find()) {
                if (!responseBodiesFirstAPI.containsKey(city)) {
                    try {
                        this.responseBody = weatherAppAPI.httpResponse(city);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    responseBodiesFirstAPI.put(city, responseBody);
                } else {
                    responseBody = responseBodiesFirstAPI.get(city);
                }
                responseBodySecondAPI = getLocalTime(city);
            }
            Platform.runLater(() -> {
                if (responseBody.equals("{\"cod\":\"400\",\"message\":\"Nothing to geocode\"}") ||
                        this.responseBody.equals("{\"cod\":\"404\",\"message\":\"city not found\"}") || !matcher.find()
                        || this.responseBodySecondAPI == null || this.responseBodySecondAPI.contains("No matching location found.")) {
                    invalidInput.setText("Enter valid city or country");
                    invalidInput.setStyle("-fx-text-fill: red;");
                    cityStartUpTextField.setStyle("-fx-text-fill: red;");
                } else {
                    if (!responseBody.equals("")) {
                        try {
                            passedFirstPage = "Passed!";
                            fetchAndDisplayWeatherData(city);
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

    private void configureGoBackToFirstPageButton() {

        Platform.runLater(() -> goBackToFirstPage.setOnAction(actionEvent -> {
            stage.setScene(firstPageScene);
            passedFirstPage = "not passed!";
            invalidInput.setText("");
            if (!firstPageVbox.getChildren().contains(fetchButton)) {
                firstPageVbox.getChildren().add(2, fetchButton);
                inputTextField.setText("");
                cityStartUpTextField.setText(city);
                cityStartUpTextField.setStyle(temperatureLabel.getStyle());
            }
        }));
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

    private void configureConvertWindSpeedButton() {
        convertWindSpeed.setOnAction(actionEvent -> {
            // Convert wind speed logic
            if (windSpeedLabel.getText().contains("km/h")) {
                windSpeedLabel.setText(String.format("Wind speed: %.0f mph", getWindSpeedInMiles(weatherData.getWind().getSpeed())));
            } else if (windSpeedLabel.getText().contains("mph")) {
                windSpeedLabel.setText(String.format("Wind speed: %.0f km/h", getWindSpeedInKms(weatherData.getWind().getSpeed())));
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
            Button convertButton = convertTemperature;
            Button showMoreButton = showMoreWeatherInfo;
            Button convertWindSpeedButton = convertWindSpeed;
            // Perform network operations, JSON parsing, and data processing here
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
                                switchVideoBackground(weatherConditionAndIcon.split("&")[1]);
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
                                    windSpeedLabel.setText(String.format("Wind speed: %.0f km/h", getWindSpeedInKms(weatherData.getWind().getSpeed())));

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
                    stage.setScene(mainScene);
                    if (inputTextField.getText().equals("")) {
                        inputTextField.setText(cityStartUpTextField.getText());
                        inputTextField.deselect();
                        Platform.runLater(() -> inputTextField.positionCaret(cityStartUpTextField.getText().length()));
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
            weatherConditionAndIcon = weatherCondition.getString("icon") + "&" + weatherCondition.get("text");
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
                    windSpeedLabel.setText(String.format("Wind speed: %.0f km/h", getWindSpeedInKms(weatherData.getWind().getSpeed())));
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