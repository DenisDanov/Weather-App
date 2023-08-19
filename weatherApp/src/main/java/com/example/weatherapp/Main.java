package com.example.weatherapp;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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
import java.util.Date;
import java.util.Objects;

public class Main extends Application {
    private final WeatherAppAPI weatherAppAPI;
    private WeatherData weatherData;
    private String city;
    private final TextField cityTextField = new TextField(); // Create a TextField for input
    private final Label temperatureLabel = new Label();
    private final Label descriptionLabel = new Label();
    private final Label temperatureFeelsLikeLabel = new Label();
    private final Label humidityLabel = new Label();
    private final Label windSpeedLabel = new Label();
    private final Label localTimeLabel = new Label();
    private final Label uvLabel = new Label();
    private final Label dateForecast = new Label();
    private final Label maxTempForecast = new Label();
    private final Label minTempForecast = new Label();
    private final Label avgTempForecast = new Label();
    private final Label maxWindForecast = new Label();
    private final Label avgHumidityForecast = new Label();
    private final Label chanceOfRainingForecast = new Label();
    private final Label chanceOfSnowForecast = new Label();
    private final Label weatherDescriptionForecast = new Label();
    private final Label sunrise = new Label();
    private final Label sunset = new Label();
    private final Button showMoreWeatherInfo = new Button("Show more weather info");
    private final Button convertTemperature = new Button("Convert temperature");
    private final Button convertWindSpeed = new Button("Convert wind speed");
    private final Button getDailyForecast = new Button("Show daily forecast");
    private final Button fetchButton = new Button("Show current weather");
    Label cityLabel = new Label("Enter City or Country:");
    Button showWeeklyForecastButton = new Button("Show weekly forecast");
    private TableView<TemperatureData> temperatureTable;

    public Main() {
        this.weatherAppAPI = new WeatherAppAPI();
    }

    Scene mainScene;
    GridPane gridPane = new GridPane();
    VBox root = createRootLayout();
    Stage stage;
    private TableView<TemperatureData> tableViewLayout;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        mainScene = new Scene(root, 666, 666);
        addStyleSheet(mainScene);
        configurePrimaryStage(primaryStage, mainScene);
        configureFetchButton(root);
        configureConvertTemperatureButton();
        configureShowMoreButton();
        configureConvertWindSpeedButton();
        configureGetDailyForecastButton();
        configureWeeklyForecastButton();
        primaryStage.show();
        stage = primaryStage;
    }

    private VBox createRootLayout() {
        root = new VBox();

        gridPane.setHgap(10);
        gridPane.setVisible(false);

        temperatureLabel.getStyleClass().add("emoji-label"); // Apply the CSS class
        descriptionLabel.getStyleClass().add("emoji-label");// Apply the CSS class
        temperatureFeelsLikeLabel.getStyleClass().add("emoji-label");
        temperatureTable = new TableView<>();
        temperatureTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Add the TableView to the root layout
        root.getChildren().addAll(cityLabel, cityTextField,
                fetchButton,
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

        showWeeklyForecastButton.setVisible(false);
        convertTemperature.setVisible(false);
        showMoreWeatherInfo.setVisible(false);
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

        // Create and configure UI components...
        return root;
    }

    private void addStyleSheet(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/mainPage.css")).toExternalForm());
        temperatureLabel.getStyleClass().add("emoji-label"); // Apply the CSS class
        descriptionLabel.getStyleClass().add("emoji-label");// Apply the CSS class
        temperatureFeelsLikeLabel.getStyleClass().add("emoji-label");
    }

    private void configurePrimaryStage(Stage primaryStage, Scene scene) {
        primaryStage.setTitle("Weather App");
        primaryStage.setScene(scene);
    }

    private void configureFetchButton(VBox root) {
        fetchButton.setOnAction(event -> {
            try {
                // Fetch and display weather data
                fetchAndDisplayWeatherData(cityTextField.getText());
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
        showWeeklyForecastButton.setOnAction(actionEvent -> {
            configureWeeklyForecastButtonAction();
        });
    }
    
    private void configureWeeklyForecastButtonAction() {
        if (root.getChildren().get(0).equals(cityLabel)) {
            resetUI();
            TableView<TemperatureData> tableView = new TableView<>();

            // Create columns for days of the week
            TableColumn<TemperatureData, String> dayColumn = new TableColumn<>("Day");
            dayColumn.setCellValueFactory(new PropertyValueFactory<>("day"));
            tableView.getColumns().add(dayColumn);
            dayColumn.setCellFactory(column -> {
                return new TableCell<TemperatureData, String>() {
                    private final Text text;

                    {
                        text = new Text();
                        text.wrappingWidthProperty().bind(dayColumn.widthProperty().subtract(4.5));
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
                };
            });
            String[] daysOfWeek = new String[7];
            JSONArray weeklyForecast = getWeeklyForecast();
            JSONObject[] daysOfTheWeek = new JSONObject[7];
            for (int i = 0; i < weeklyForecast.length(); i++) {
                JSONObject day = weeklyForecast.getJSONObject(i);
                daysOfWeek[i] = formatDateTime(day.get("date").toString());
                daysOfTheWeek[i] = weeklyForecast.getJSONObject(i);
            }
            // Add day columns
            for (String day : daysOfWeek) {
                TableColumn<TemperatureData, String> columns = new TableColumn<>(day);
                columns.setCellValueFactory(new PropertyValueFactory<>(day.toLowerCase())); // Matches with the property name in TemperatureData
                columns.setResizable(true);
                columns.setCellFactory(column -> {
                    return new TableCell<TemperatureData, String>() {
                        private final Text text;

                        {
                            text = new Text();
                            text.wrappingWidthProperty().bind(columns.widthProperty());
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
                    };
                });
                tableView.getColumns().add(columns);

            }

            JSONObject day1 = daysOfTheWeek[0];
            JSONObject day2 = daysOfTheWeek[1];
            JSONObject day3 = daysOfTheWeek[2];
            JSONObject day4 = daysOfTheWeek[3];
            JSONObject day5 = daysOfTheWeek[4];
            JSONObject day6 = daysOfTheWeek[5];
            JSONObject day7 = daysOfTheWeek[6];

            tableView.getItems().add(new TemperatureData("Max Temperature",
                    (day1.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                    (day2.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                    (day3.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                    (day4.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                    (day5.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                    (day6.getJSONObject("day").getDouble("maxtemp_c") + "°C"),
                    (day7.getJSONObject("day").getDouble("maxtemp_c") + "°C")));
            tableView.getItems().add(new TemperatureData("Min Temperature",
                    (day1.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                    (day2.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                    (day3.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                    (day4.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                    (day5.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                    (day6.getJSONObject("day").getDouble("mintemp_c") + "°C"),
                    (day7.getJSONObject("day").getDouble("mintemp_c") + "°C")));
            tableView.getItems().add(new TemperatureData("Avg Temperature",
                    (day1.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                    (day2.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                    (day3.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                    (day4.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                    (day5.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                    (day6.getJSONObject("day").getDouble("avgtemp_c") + "°C"),
                    (day7.getJSONObject("day").getDouble("avgtemp_c") + "°C")));
            tableView.getItems().add(new TemperatureData("Max Wind Speed",
                    (day1.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                    (day2.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                    (day3.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                    (day4.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                    (day5.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                    (day6.getJSONObject("day").getDouble("maxwind_kph") + " km/h"),
                    (day7.getJSONObject("day").getDouble("maxwind_kph") + " km/h")));
            tableView.getItems().add(new TemperatureData("Avg Humidity",
                    (day1.getJSONObject("day").getDouble("maxwind_kph") + "%"),
                    (day2.getJSONObject("day").getDouble("maxwind_kph") + "%"),
                    (day3.getJSONObject("day").getDouble("maxwind_kph") + "%"),
                    (day4.getJSONObject("day").getDouble("maxwind_kph") + "%"),
                    (day5.getJSONObject("day").getDouble("maxwind_kph") + "%"),
                    (day6.getJSONObject("day").getDouble("maxwind_kph") + "%"),
                    (day7.getJSONObject("day").getDouble("maxwind_kph") + "%")));
            tableView.getItems().add(new TemperatureData("UV Index",
                    getUvOutputFormat(day1.getJSONObject("day").getDouble("avghumidity")),
                    getUvOutputFormat(day2.getJSONObject("day").getDouble("avghumidity")),
                    getUvOutputFormat(day3.getJSONObject("day").getDouble("avghumidity")),
                    getUvOutputFormat(day4.getJSONObject("day").getDouble("avghumidity")),
                    getUvOutputFormat(day5.getJSONObject("day").getDouble("avghumidity")),
                    getUvOutputFormat(day6.getJSONObject("day").getDouble("avghumidity")),
                    getUvOutputFormat(day7.getJSONObject("day").getDouble("avghumidity"))));
            tableView.getItems().add(new TemperatureData("Chance of Rain",
                    (day1.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                    (day2.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                    (day3.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                    (day4.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                    (day5.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                    (day6.getJSONObject("day").getDouble("daily_chance_of_rain") + "%"),
                    (day7.getJSONObject("day").getDouble("daily_chance_of_rain") + "%")));
            tableView.getItems().add(new TemperatureData("Chance of Snow",
                    (day1.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                    (day2.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                    (day3.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                    (day4.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                    (day5.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                    (day6.getJSONObject("day").getDouble("daily_chance_of_snow") + "%"),
                    (day7.getJSONObject("day").getDouble("daily_chance_of_snow") + "%")));
            tableView.getItems().add(new TemperatureData("Weather Description",
                    (day1.getJSONObject("day").getJSONObject("condition").getString("text")),
                    (day2.getJSONObject("day").getJSONObject("condition").getString("text")),
                    (day3.getJSONObject("day").getJSONObject("condition").getString("text")),
                    (day4.getJSONObject("day").getJSONObject("condition").getString("text")),
                    (day5.getJSONObject("day").getJSONObject("condition").getString("text")),
                    (day6.getJSONObject("day").getJSONObject("condition").getString("text")),
                    (day7.getJSONObject("day").getJSONObject("condition").getString("text"))));
            tableView.getItems().add(new TemperatureData("Sunrise",
                    (day1.getJSONObject("astro").getString("sunrise")),
                    (day2.getJSONObject("astro").getString("sunrise")),
                    (day3.getJSONObject("astro").getString("sunrise")),
                    (day4.getJSONObject("astro").getString("sunrise")),
                    (day5.getJSONObject("astro").getString("sunrise")),
                    (day6.getJSONObject("astro").getString("sunrise")),
                    (day7.getJSONObject("astro").getString("sunrise"))));
            tableView.getItems().add(new TemperatureData("Sunset",
                    (day1.getJSONObject("astro").getString("sunset")),
                    (day2.getJSONObject("astro").getString("sunset")),
                    (day3.getJSONObject("astro").getString("sunset")),
                    (day4.getJSONObject("astro").getString("sunset")),
                    (day5.getJSONObject("astro").getString("sunset")),
                    (day6.getJSONObject("astro").getString("sunset")),
                    (day7.getJSONObject("astro").getString("sunset"))));

            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            double tableViewWidth = 0;
            for (TableColumn<?, ?> column : tableView.getColumns()) {
                tableViewWidth += column.getWidth();
            }
            tableView.setPrefHeight(470);
            VBox root = new VBox(tableView);
            Scene scene = new Scene(root, tableViewWidth, 650); // Adjusted scene dimensions
            Button getToMainPage = new Button("Go back to the main page");
            root.getChildren().add(getToMainPage);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/weeklyForecastPage.css")).toExternalForm());
            stage.setScene(scene);

            stage.show();

            getToMainPage.setOnAction(actionEvent -> {
                returnToMainPage();
            });
        } else {
            returnToMainPage();
        }
    }


    public static class TemperatureData {
        private String day;
        private String monday, tuesday, wednesday, thursday, friday, saturday, sunday;

        public TemperatureData(String day, String monday, String tuesday, String wednesday, String thursday, String friday, String saturday, String sunday) {
            this.day = day;
            this.monday = monday;
            this.tuesday = tuesday;
            this.wednesday = wednesday;
            this.thursday = thursday;
            this.friday = friday;
            this.saturday = saturday;
            this.sunday = sunday;
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
            ForecastData forecastData = getDailyForecast();
            if (dateForecast.getText().equals("")) {
                getDailyForecast.setVisible(true);
                convertWindSpeed.setVisible(true);
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
                showWeeklyForecastButton.setVisible(true);
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
                showWeeklyForecastButton.setVisible(false);
                dateForecast.setText("");
                weatherDescriptionForecast.setText("");
                maxTempForecast.setText("");
                minTempForecast.setText("");
                avgTempForecast.setText("");
                maxWindForecast.setText("");
                avgHumidityForecast.setText("");
                chanceOfRainingForecast.setText("");
                chanceOfSnowForecast.setText("");
                sunrise.setText("");
                sunset.setText("");
            }
        });
    }

    private void fetchAndDisplayWeatherData(String cityTextField) throws IOException {
        // Fetch and display weather data logic
        Button convertButton = convertTemperature;
        convertButton.setVisible(true);
        Button showMoreButton = showMoreWeatherInfo;
        Button convertWindSpeedButton = convertWindSpeed;
        showMoreButton.setVisible(true);
        humidityLabel.setVisible(true);
        windSpeedLabel.setVisible(true);
        uvLabel.setVisible(true);

        this.city = cityTextField;
        try {
            String responseBody = weatherAppAPI.httpResponse(city);
            System.out.println(responseBody);
            Gson gson = new Gson();
            weatherData = gson.fromJson(responseBody, WeatherData.class);

            MainParsedData mainInfo = weatherData.getMain();
            WeatherInfo[] weatherInfo = weatherData.getWeather();

            if (mainInfo != null && weatherInfo != null && weatherInfo.length > 0) {
                temperatureLabel.setVisible(true);
                descriptionLabel.setVisible(true);
                temperatureFeelsLikeLabel.setVisible(true);
                double temperatureCelsius = getTempInCelsius(mainInfo.getTemp());
                double temperatureFeelsLikeCelsius = getTempInCelsius(mainInfo.getFeels_like());
                String description = weatherInfo[0].getDescription();

                localTimeLabel.setText(String.format("Local time: %s", formatDateTime(getLocalTime(city))));
                temperatureLabel.setText(String.format("Temperature: %.0f°C \uD83C\uDF21", temperatureCelsius));
                temperatureFeelsLikeLabel.setText(String.format("Feels like: %.0f°C \uD83C\uDF21", temperatureFeelsLikeCelsius));

                if (description.contains("cloud")) {
                    descriptionLabel.setText("Weather Description: " + description + " ☁️"); // Emoji added here
                } else if (description.contains("rain")) {
                    descriptionLabel.setText("Weather Description: " + description + " \uD83C\uDF27️");
                } else {
                    descriptionLabel.setText("Weather Description: " + description + " ☀️");
                }
                if (!humidityLabel.getText().equals("") && !windSpeedLabel.getText().equals("")) {
                    if (humidityLabel.getText().equals("") && windSpeedLabel.getText().equals("")) {
                        humidityLabel.setText(String.format("Humidity: %d%%", mainInfo.getHumidity()));
                        uvLabel.setText("UV Index: " + getUvOutputFormat(getUV(city)));
                        windSpeedLabel.setText(String.format("Wind speed: %.2f km/h", getWindSpeedInKms(weatherData.getWind().getSpeed())));
                        convertWindSpeedButton.setVisible(true);
                        getDailyForecast.setVisible(true);
                    } else {
                        getDailyForecast.setVisible(true);
                        convertWindSpeedButton.setVisible(true);
                        ForecastData forecastData = getDailyForecast();
                        humidityLabel.setText(String.format("Humidity: %d%%", mainInfo.getHumidity()));
                        uvLabel.setText("UV Index: " + getUvOutputFormat(getUV(city)));
                        windSpeedLabel.setText(String.format("Wind speed: %.2f km/h", getWindSpeedInKms(weatherData.getWind().getSpeed())));
                        if (!dateForecast.getText().equals("")) {
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
                            dateForecast.setText("");
                            weatherDescriptionForecast.setText("");
                            maxTempForecast.setText("");
                            minTempForecast.setText("");
                            avgTempForecast.setText("");
                            maxWindForecast.setText("");
                            avgHumidityForecast.setText("");
                            chanceOfRainingForecast.setText("");
                            chanceOfSnowForecast.setText("");
                            sunrise.setText("");
                            sunset.setText("");
                        }
                    }
                }
            } else {
                localTimeLabel.setText("Invalid place.");
                showWeeklyForecastButton.setVisible(false);
                temperatureLabel.setVisible(false);
                descriptionLabel.setVisible(false);
                temperatureFeelsLikeLabel.setVisible(false);
                convertButton.setVisible(false);
                showMoreButton.setVisible(false);
                humidityLabel.setVisible(false);
                windSpeedLabel.setVisible(false);
                convertWindSpeedButton.setVisible(false);
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
                dateForecast.setText("");
                weatherDescriptionForecast.setText("");
                maxTempForecast.setText("");
                minTempForecast.setText("");
                avgTempForecast.setText("");
                maxWindForecast.setText("");
                avgHumidityForecast.setText("");
                chanceOfRainingForecast.setText("");
                chanceOfSnowForecast.setText("");
                sunrise.setText("");
                sunset.setText("");
                humidityLabel.setText("");
                uvLabel.setText("");
                windSpeedLabel.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            temperatureLabel.setText("An error occurred.");
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
          /*
        cityLabel -> gridPaneWeekDays
        cityTextField -> MaxTempLabel
        fetchButton -> MinTempLabel
        localTimeLabel -> AvgTempLabel
        temperatureLabel -> MaxWindLabel
        temperatureFeelsLikeLabel -> AvgHumidityLabel
        descriptionLabel -> ChanceOfRaining
        convertTemperature -> ChanceOfSnowing
        showMoreWeatherInfo -> WeatherDescription
        humidityLabel -> Sunrise
        uvLabel -> Sunset
         */
        stage.setScene(mainScene);
        stage.show();
        gridPane.setVisible(false);
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
        cityTextField.setVisible(true);
        fetchButton.setVisible(true);
        cityLabel.setVisible(true);
    }

    private void resetUI() {
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
        cityTextField.setVisible(false);
        fetchButton.setVisible(false);
        cityLabel.setVisible(false);
    }

    private JSONArray getWeeklyForecast() {
        String responseBody;
        try {
            responseBody = ForecastAPI.httpResponseForecast(city);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JSONObject response = new JSONObject(responseBody);
        JSONArray forecastDays = response.getJSONObject("forecast").getJSONArray("forecastday");
        System.out.println(forecastDays.length());
        return forecastDays;
    }

    private ForecastData getDailyForecast() {
        String responseBody;
        try {
            responseBody = ForecastAPI.httpResponseForecast(city);
        } catch (IOException e) {
            throw new RuntimeException(e);
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

        ForecastData forecastData = new ForecastData(date, maxTempC, minTempC, avgTempC, maxWind,
                avgHumidity, chanceOfRain, chanceOfSnow, weatherCondition, sunRise, sunSet);
        return forecastData;
    }

    private double getUV(String city) {

        String responseBody = null;
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
        MainParsedData mainInfo = weatherData.getMain();
        double uvIndex = getUV(city);
        if (humidityLabel.getText().equals("") && windSpeedLabel.getText().equals("") &&
                uvLabel.getText().equals("")) {
            humidityLabel.setText(String.format("Humidity: %d%%", mainInfo.getHumidity()));
            uvLabel.setText("UV Index: " + getUvOutputFormat(uvIndex));
            windSpeedLabel.setText(String.format("Wind speed: %.2f km/h", getWindSpeedInKms(weatherData.getWind().getSpeed())));
            convertWindSpeed.setVisible(true);
            getDailyForecast.setVisible(true);
        } else {
            humidityLabel.setText("");
            windSpeedLabel.setText("");
            uvLabel.setText("");
            convertWindSpeed.setVisible(false);
            getDailyForecast.setVisible(false);
            showWeeklyForecastButton.setVisible(false);
            dateForecast.setText("");
            maxTempForecast.setText("");
            minTempForecast.setText("");
            avgTempForecast.setText("");
            maxWindForecast.setText("");
            avgHumidityForecast.setText("");
            chanceOfRainingForecast.setText("");
            chanceOfSnowForecast.setText("");
            weatherDescriptionForecast.setText("");
            sunrise.setText("");
            sunset.setText("");
        }
    }

    private String getLocalTime(String city) {
        String responseBody = null;
        try {
            responseBody = ForecastAPI.httpResponse(city);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Gson gson = new Gson();
        System.out.println(responseBody);
        ForecastAPIData forecastData = gson.fromJson(responseBody, ForecastAPIData.class);
        return forecastData.getLocation().getLocaltime();
    }

    public static String formatDateTime(String inputDateTime) {
        // Date formatting logic
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE");

        try {
            Date date = inputFormat.parse(inputDateTime);
            String dayOfWeek = outputFormat.format(date);
            return dayOfWeek;
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
}

