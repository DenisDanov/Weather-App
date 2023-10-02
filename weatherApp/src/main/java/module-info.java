module weatherAppModule {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires mysql.connector.j;
    requires com.google.gson;
    requires org.json;
    requires com.fasterxml.jackson.core;
    requires java.sql;
    opens parsingWeatherData;
    opens weatherApi;
    exports com.example.weatherapp;
    exports parsingWeatherData;
    exports com.example.weatherapp.labels;
    exports com.example.weatherapp.weeklyForecastTable;
}

