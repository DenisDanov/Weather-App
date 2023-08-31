package com.example.weatherapp;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.*;
import java.time.format.*;
import java.util.*;

public class SevenDayTableApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create a TableView
        TableView<DayData> table = new TableView<>();

        // Create a "Data Type" column
        TableColumn<DayData, String> dataTypeColumn = new TableColumn<>("Data Type");
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
        dataTypeColumn.setEditable(false);

        // Create columns for each day of the week (including current day)
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEE");
        for (int i = 0; i < 7; i++) {
            final int index = i;
            TableColumn<DayData, String> column = new TableColumn<>(currentDate.format(dateFormatter));
            column.setCellValueFactory(data -> {
                int rowIndex = data.getTableView().getItems().indexOf(data.getValue());
                return new SimpleStringProperty(data.getValue().getData(rowIndex, index));
            });
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setOnEditCommit(event -> {
                DayData dayData = event.getRowValue();
                dayData.setData(event.getTablePosition().getRow(), index, event.getNewValue());
            });
            table.getColumns().add(column);
            currentDate = currentDate.plusDays(1);
        }

        // Add columns to the table
        table.getColumns().add(0, dataTypeColumn);

        // Create and add sample data
        ObservableList<DayData> data = FXCollections.observableArrayList();
        for (int i = 0; i < 11; i++) {
            data.add(new DayData());
        }
        // Add sample temperature data for the second row (Min Temp) and third row (Avg Temp)
        data.get(1).setTemperature(0, "25"); // Example Min Temp
        data.get(2).setTemperature(0, "20"); // Example Avg Temp
        table.setItems(data);

        // Create a layout
        VBox vbox = new VBox(table);
        Scene scene = new Scene(vbox, 800, 400);

        primaryStage.setTitle("Seven Day Table");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class DayData {
        private final List<SimpleStringProperty> temperatures;
        private final List<List<SimpleStringProperty>> data;

        public DayData() {
            temperatures = new ArrayList<>(7);
            data = new ArrayList<>(11);

            for (int i = 0; i < 7; i++) {
                temperatures.add(new SimpleStringProperty(""));
            }

            for (int i = 0; i < 11; i++) {
                List<SimpleStringProperty> row = new ArrayList<>(7);
                for (int j = 0; j < 7; j++) {
                    row.add(new SimpleStringProperty(""));
                }
                data.add(row);
            }
        }

        public String getTemperature(int index) {
            return temperatures.get(index).get();
        }

        public void setTemperature(int index, String value) {
            temperatures.get(index).set(value);
        }

        public String getData(int rowIndex, int columnIndex) {
            return data.get(rowIndex).get(columnIndex).get();
        }

        public String setData(int rowIndex, int columnIndex, String value) {
            data.get(rowIndex).get(columnIndex).set(value);
            return value;
        }
    }
}
