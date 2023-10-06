package parsingWeatherData;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Condition {
    private String text;
    private String icon;
    public String getText() {
        return text;
    }

    public String getIcon() {
        return icon;
    }
// Getter and setter methods for text and icon
}
