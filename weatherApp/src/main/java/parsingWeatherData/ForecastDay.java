package parsingWeatherData;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastDay {
    private String date;
    private Day day;
    private Astro astro;

    public String getDate() {
        return date;
    }

    public Day getDay() {
        return day;
    }

    public Astro getAstro() {
        return astro;
    }


}
