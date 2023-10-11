package parsingWeatherData;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Current {

    private double temp_c;
    private Condition condition;
    @JsonProperty("wind_kph")
    private double windKph;
    private int humidity;
    @JsonProperty("feelslike_c")
    private double feelsLikeC;
    private double uv;

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
}
