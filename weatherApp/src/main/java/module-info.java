module weatherAppModule {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires com.google.gson;
    requires vertx.json;
    requires vertx.json.value.mapper;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.json;
    requires java.sql;
    opens parsingWeatherData;
    opens weatherApi;
    exports com.example.weatherapp;
    exports parsingWeatherData;
    exports com.example.weatherapp.labels;
    exports com.example.weatherapp.weeklyForecastTable;
}

