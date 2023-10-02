package parsingWeatherData;

import com.google.gson.annotations.SerializedName;

public class Current {

    @SerializedName("temp_c")
    private double temp_c;

    @SerializedName("humidity")
    private int humidity;

    @SerializedName("feelslike_c")
    private double feelslike_c;

    @SerializedName("wind_kph")
    private double wind_kph;

    public double getSpeed() {
        return wind_kph;
    }

    public double getTemp() {
        return temp_c;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getFeels_like() {
        return feelslike_c;
    }
}
