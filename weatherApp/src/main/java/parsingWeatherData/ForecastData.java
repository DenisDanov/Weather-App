package parsingWeatherData;

public class ForecastData {

    private String date;
    private double maxTemp;
    private double minTemp;
    private double uvIndex;
    private double maxWind;
    private double avgHumidity;
    private int percentChanceOfRain;
    private int percentChanceOfSnow;
    private String weatherDescription;
    private String sunRise;
    private String sunSet;

    public ForecastData(String date,
            double maxTemp,
            double minTemp,
            double uvIndex,
            double maxWind,
            double avgHumidity,
            int percentChanceOfRain,
            int percentChanceOfSnow,
            String weatherDescription,
            String sunRise,
            String sunSet) {

        this.date = date;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.uvIndex = uvIndex;
        this.maxWind = maxWind;
        this.avgHumidity = avgHumidity;
        this.percentChanceOfRain = percentChanceOfRain;
        this.percentChanceOfSnow = percentChanceOfSnow;
        this.weatherDescription = weatherDescription;
        this.sunRise = sunRise;
        this.sunSet = sunSet;
    }

    public String getDate() {
        return date;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public double getUvIndex() {
        return uvIndex;
    }

    public double getMaxWind() {
        return maxWind;
    }

    public double getAvgHumidity() {
        return avgHumidity;
    }

    public int getPercentChanceOfRain() {
        return percentChanceOfRain;
    }

    public int getPercentChanceOfSnow() {
        return percentChanceOfSnow;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public String getSunRise() {
        return sunRise;
    }

    public String getSunSet() {
        return sunSet;
    }
}
