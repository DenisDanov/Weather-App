package parsingWeatherData;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Day {
    @JsonProperty("maxtemp_c")
    private double maxTempC;
    @JsonProperty("mintemp_c")
    private double minTempC;
    @JsonProperty("avgtemp_c")
    private double avgTempC;
    @JsonProperty("maxwind_kph")
    private double maxWindKph;
    @JsonProperty("totalsnow_cm")
    private double totalSnowCm;
    @JsonProperty("avghumidity")
    private double avgHumidity;
    @JsonProperty("daily_chance_of_rain")
    private int dailyChanceOfRain;
    @JsonProperty("daily_chance_of_snow")
    private int dailyChanceOfSnow;

    public double getMaxTempC() {
        return maxTempC;
    }

    public double getMinTempC() {
        return minTempC;
    }

    public double getAvgTempC() {
        return avgTempC;
    }

    public double getMaxWindKph() {
        return maxWindKph;
    }

    public double getTotalSnowCm() {
        return totalSnowCm;
    }

    public double getAvgHumidity() {
        return avgHumidity;
    }

    public int getDailyChanceOfRain() {
        return dailyChanceOfRain;
    }

    public int getDailyChanceOfSnow() {
        return dailyChanceOfSnow;
    }

    public Condition getCondition() {
        return condition;
    }

    public double getUv() {
        return uv;
    }

    private Condition condition;
    private double uv;

    // Getter and setter methods for day fields
}
