package parsingWeatherData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Current {
    @JsonProperty("last_updated")
    private String lastUpdated;
    private double temp_c;
    private Condition condition;

    public String getLastUpdated() {
        return lastUpdated;
    }

    public double getTemp_c() {
        return temp_c;
    }

    public Condition getCondition() {
        return condition;
    }

    public double getWindKph() {
        return windKph;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getFeelsLikeC() {
        return feelsLikeC;
    }

    public double getUv() {
        return uv;
    }

    @JsonProperty("wind_kph")
    private double windKph;
    private int humidity;
    @JsonProperty("feelslike_c")
    private double feelsLikeC;
    private double uv;

    // Getter and setter methods for current fields
}
