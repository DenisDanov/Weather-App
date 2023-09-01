package com.example.weatherapp;
import javafx.beans.property.*;
import java.util.*;

public class WeeklyForecastTable {
    private final List<SimpleStringProperty> temperatures;
    private final List<List<SimpleStringProperty>> data;
    public WeeklyForecastTable() {
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