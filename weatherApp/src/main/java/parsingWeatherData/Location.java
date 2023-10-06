package parsingWeatherData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {
    private String localtime;

    public String getLocaltime() {
        return localtime;
    }
// Getter and setter methods for location fields
}
