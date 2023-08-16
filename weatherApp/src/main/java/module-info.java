module weatherApp {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires com.google.gson;
    requires org.json;
    opens parsingWeatherData;
    opens weatherApi;
    exports com.example.weatherapp;
}

