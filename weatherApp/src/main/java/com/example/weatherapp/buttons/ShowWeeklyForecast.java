package com.example.weatherapp.buttons;

import com.example.weatherapp.labels.BubbleLabels;
import com.example.weatherapp.Main;
import com.example.weatherapp.weeklyForecastTable.WeeklyForecastTable;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import weatherApi.ForecastAPI;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ShowWeeklyForecast extends Button {

    private VBox root;
    private final Label cityLabel;
    private final ConcurrentHashMap<String, String> responseBodiesSecondAPI;
    private String city;
    private final BubbleLabels humidityLabel;
    private final BubbleLabels windSpeedLabel;
    private final BubbleLabels uvLabel;
    private ShowDailyForecast getDailyForecast;
    private final Label dateForecast;
    private final Label maxTempForecast;
    private final Label minTempForecast;
    private final Label avgTempForecast;
    private final Label maxWindForecast;
    private final Label avgHumidityForecast;
    private final Label chanceOfRainingForecast;
    private final Label chanceOfSnowForecast;
    private final Label weatherDescriptionForecast;
    private final Label sunrise;
    private final Label sunset;
    private final ConvertWindSpeed convertWindSpeed;
    private final TextField inputTextField;
    private final BubbleLabels localTimeLabel;
    private final BubbleLabels temperatureLabel;
    private final BubbleLabels descriptionLabel;
    private final BubbleLabels temperatureFeelsLikeLabel;
    private ShowMoreWeatherData showMoreWeatherInfo;
    private final ConvertTemperature convertTemperature;
    private final Button fetchButton;
    private Scene mainScene;
    private Stage stage;

    public ShowWeeklyForecast(VBox root,
                              Label cityLabel,
                              ConcurrentHashMap<String, String> responseBodiesSecondAPI,
                              String city, BubbleLabels humidityLabel,
                              BubbleLabels windSpeedLabel,
                              BubbleLabels uvLabel,
                              ShowDailyForecast getDailyForecast,
                              Label dateForecast,
                              Label maxTempForecast,
                              Label minTempForecast,
                              Label avgTempForecast,
                              Label maxWindForecast,
                              Label avgHumidityForecast,
                              Label chanceOfRainingForecast,
                              Label chanceOfSnowForecast,
                              Label weatherDescriptionForecast,
                              Label sunrise,
                              Label sunset,
                              ConvertWindSpeed convertWindSpeed,
                              TextField inputTextField,
                              BubbleLabels localTimeLabel,
                              BubbleLabels temperatureLabel,
                              BubbleLabels descriptionLabel,
                              BubbleLabels temperatureFeelsLikeLabel,
                              ShowMoreWeatherData showMoreWeatherInfo,
                              ConvertTemperature convertTemperature,
                              Button fetchButton,
                              Scene mainScene,
                              Stage stage) {

        this.setRoot(root);
        this.cityLabel = cityLabel;
        this.responseBodiesSecondAPI = responseBodiesSecondAPI;
        this.setCity(city);
        this.humidityLabel = humidityLabel;
        this.windSpeedLabel = windSpeedLabel;
        this.uvLabel = uvLabel;
        this.setGetDailyForecast(getDailyForecast);
        this.dateForecast = dateForecast;
        this.maxTempForecast = maxTempForecast;
        this.minTempForecast = minTempForecast;
        this.avgTempForecast = avgTempForecast;
        this.maxWindForecast = maxWindForecast;
        this.avgHumidityForecast = avgHumidityForecast;
        this.chanceOfRainingForecast = chanceOfRainingForecast;
        this.chanceOfSnowForecast = chanceOfSnowForecast;
        this.weatherDescriptionForecast = weatherDescriptionForecast;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.convertWindSpeed = convertWindSpeed;
        this.inputTextField = inputTextField;
        this.localTimeLabel = localTimeLabel;
        this.temperatureLabel = temperatureLabel;
        this.descriptionLabel = descriptionLabel;
        this.temperatureFeelsLikeLabel = temperatureFeelsLikeLabel;
        this.setShowMoreWeatherInfo(showMoreWeatherInfo);
        this.convertTemperature = convertTemperature;
        this.fetchButton = fetchButton;
        this.setMainScene(mainScene);
        this.setStage(stage);

        configureButton();
    }

    private void configureButton() {
        setOnAction(actionEvent -> showWeeklyForecastAction());
    }

    public void setGetDailyForecast(ShowDailyForecast getDailyForecast) {
        this.getDailyForecast = getDailyForecast;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setShowMoreWeatherInfo(ShowMoreWeatherData showMoreWeatherInfo) {
        this.showMoreWeatherInfo = showMoreWeatherInfo;
    }

    public void setRoot(VBox root) {
        this.root = root;
    }

    public void setMainScene(Scene mainScene) {
        this.mainScene = mainScene;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void showWeeklyForecastAction() {
        if (root.getChildren().get(0).equals(cityLabel)) {
            resetUI();
            JSONArray weeklyForecast = getWeeklyForecast();
            JSONObject[] daysOfTheWeek = new JSONObject[7];
            for (int i = 0; i < weeklyForecast.length(); i++) {
                daysOfTheWeek[i] = weeklyForecast.getJSONObject(i);
            }

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
            TableView<WeeklyForecastTable> table = new TableView<>();

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
                fixTruncatedText(column);
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

            addDataToTable(data,daysOfTheWeek);
            table.setItems(data);
            fixTruncatedText(dataTypeColumn);

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

    private void fixTruncatedText(TableColumn<WeeklyForecastTable, String> column){
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
    }

    private void addDataToTable(ObservableList<WeeklyForecastTable> data, JSONObject[] daysOfTheWeek){
        JSONObject day1 = daysOfTheWeek[0];
        JSONObject day2 = daysOfTheWeek[1];
        JSONObject day3 = daysOfTheWeek[2];
        JSONObject day4 = daysOfTheWeek[3];
        JSONObject day5 = daysOfTheWeek[4];
        JSONObject day6 = daysOfTheWeek[5];
        JSONObject day7 = daysOfTheWeek[6];
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

        data.get(5).setData(5, 0, Main.getUvOutputFormat(day1.getJSONObject("day").getDouble("uv")));
        data.get(5).setData(5, 1, Main.getUvOutputFormat(day2.getJSONObject("day").getDouble("uv")));
        data.get(5).setData(5, 2, Main.getUvOutputFormat(day3.getJSONObject("day").getDouble("uv")));
        data.get(5).setData(5, 3, Main.getUvOutputFormat(day4.getJSONObject("day").getDouble("uv")));
        data.get(5).setData(5, 4, Main.getUvOutputFormat(day5.getJSONObject("day").getDouble("uv")));
        data.get(5).setData(5, 5, Main.getUvOutputFormat(day6.getJSONObject("day").getDouble("uv")));
        data.get(5).setData(5, 6, Main.getUvOutputFormat(day7.getJSONObject("day").getDouble("uv")));

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
}
