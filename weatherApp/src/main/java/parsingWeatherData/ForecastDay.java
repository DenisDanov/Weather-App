package parsingWeatherData;

import java.util.List;

public class ForecastDay {
    private String date;
    private Day day;
    private Astro astro;
    private List<Hour> hour;

    public String getDate() {
        return date;
    }

    public Day getDay() {
        return day;
    }

    public Astro getAstro() {
        return astro;
    }

    public List<Hour> getHour() {
        return hour;
    }
// Getter and setter methods for date, day, astro, and hour
}
