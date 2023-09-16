package com.example.weatherapp.dynamicBackground;

import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;
import weatherApi.ForecastAPI;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.example.weatherapp.Main.formatDateToDayAndHour;
import static com.example.weatherapp.Main.getLocalTime;

public class DynamicBackgroundImpl {

    private String lastWeatherDescription;
    private String lastTimeCheck;
    private String city;
    private ConcurrentHashMap<String, String> responseBodiesSecondAPI;
    private String responseBodyGetSunsetSunrise;
    private MediaPlayer mediaPlayer;
    private final MediaView mediaView = new MediaView();
    private final FadeTransition fadeOut;
    private final FadeTransition fadeIn;
    private MediaPlayer newMediaPlayer;
    private Stage stage;
    private Scene mainScene;

    public DynamicBackgroundImpl(StackPane rootLayout,
                                 VBox root,
                                 String city,
                                 ConcurrentHashMap<String, String> responseBodiesSecondAPI,
                                 Stage stage,
                                 Scene mainScene) {
        this.lastWeatherDescription = "";
        this.lastTimeCheck = "";
        this.setCity(city);
        this.setResponseBodiesSecondAPI(responseBodiesSecondAPI);
        this.responseBodyGetSunsetSunrise = "";
        this.setStage(stage);
        this.setMainScene(mainScene);
        this.fadeOut = new FadeTransition();
        this.fadeIn = new FadeTransition();
        Objects.requireNonNull(rootLayout).getChildren().addAll(mediaView, root);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setMainScene(Scene mainScene) {
        this.mainScene = mainScene;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setResponseBodiesSecondAPI(ConcurrentHashMap<String, String> responseBodiesSecondAPI) {
        this.responseBodiesSecondAPI = responseBodiesSecondAPI;
    }

    private void fadeInToNewVideo() {
        mediaView.getMediaPlayer().dispose();
        mediaView.setMediaPlayer(newMediaPlayer);

        fadeIn.setNode(mediaView);
        fadeIn.setFromValue(1.0);
        fadeIn.setToValue(1.0);

        // Start the fade-in animation
        fadeIn.play();

        newMediaPlayer.play();
        System.out.println(fadeIn.getDuration());
    }

    private MediaPlayer createMediaPlayer(String resourcePath) {

        if (mediaView.getMediaPlayer() != null) {
            // Create a new MediaPlayer for the second video
            newMediaPlayer = new MediaPlayer(
                    new Media(Objects.requireNonNull(getClass().getResource(resourcePath)).toString()));
            newMediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            newMediaPlayer.setMute(true);

            fadeOut.setNode(mediaView);
            if (stage.getScene() == mainScene) {
                fadeOut.setDuration(Duration.seconds(0.1));
                fadeIn.setDuration(Duration.seconds(0.1));
            } else {
                fadeOut.setDuration(Duration.millis(1));
                fadeIn.setDuration(Duration.millis(1));
            }

            // Create a crossfade transition when switching between videos
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(1.0);

            // Set an event handler for when the fade out animation is finished
            fadeOut.setOnFinished(event -> {
                // Crossfade to the second video
                fadeInToNewVideo();
            });

            // Start the fade-out animation
            fadeOut.play();
            System.out.println(fadeOut.getDuration());
        } else {
            mediaPlayer = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource(resourcePath)).toString()));
            mediaPlayer.setAutoPlay(false); // Prevent immediate playback
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setMute(true);

            mediaView.setMediaPlayer(mediaPlayer);
            mediaView.setSmooth(true);
            mediaView.setVisible(true);

            mediaPlayer.play();
        }
        return mediaPlayer;
    }

    public void switchVideoBackground(String weatherDescription) {
        boolean currentTimeIsLaterThanSunsetVar = currentTimeIsLaterThanSunset();
        if (!lastWeatherDescription.equals(weatherDescription)) {
            playDesiredVideo(weatherDescription, currentTimeIsLaterThanSunsetVar);
        } else {
            if (!currentTimeIsLaterThanSunsetVar && lastTimeCheck.equals("Day")) {
                playDesiredVideo(weatherDescription, false);
            } else if (currentTimeIsLaterThanSunsetVar && lastTimeCheck.equals("Night")) {
                playDesiredVideo(weatherDescription, true);
            }
        }
        if (!lastWeatherDescription.equals(weatherDescription)) {
            lastWeatherDescription = weatherDescription;
        }
        if (!currentTimeIsLaterThanSunsetVar) {
            lastTimeCheck = "Night";
        } else {
            lastTimeCheck = "Day";
        }
    }

    public void playDesiredVideo(String weatherDescription, boolean currentTimeIsLaterThanSunsetVar) {
        if (weatherDescription.toLowerCase().contains("light rain") && currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-LightRain-Day.mp4");
        } else if (weatherDescription.toLowerCase().contains("cloud") && !currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-Cloudy-Night.mp4");
        } else if (weatherDescription.toLowerCase().contains("overcast") && currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-Overcast-Day.mp4");
        } else if (weatherDescription.toLowerCase().contains("clear") && !currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-Clear-Night.mp4");
        } else if (weatherDescription.toLowerCase().contains("clear") && currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-Clear-Day.mp4");
        } else if (weatherDescription.toLowerCase().contains("light rain") && !currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-LightRain-Night.mp4");
        } else if (weatherDescription.toLowerCase().contains("heavy rain") && currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-HeavyRain-Day.mp4");
        } else if (weatherDescription.toLowerCase().contains("heavy rain") && !currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-HeavyRain-Night.mp4");
        } else if (weatherDescription.toLowerCase().contains("sunny") && currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-Clear-Day.mp4");
        } else if (weatherDescription.toLowerCase().contains("rain") && currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-HeavyRain-Day.mp4");
        } else if (weatherDescription.toLowerCase().contains("rain") && !currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-HeavyRain-Night.mp4");
        } else if (weatherDescription.toLowerCase().contains("overcast") && !currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-Overcast-Night.mp4");
        } else if (weatherDescription.toLowerCase().contains("cloud") && currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-Cloudy-Day.mp4");
        } else if (weatherDescription.toLowerCase().contains("mist") && currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-Overcast-Day.mp4");
        } else if (weatherDescription.toLowerCase().contains("fog") && currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-Overcast-Day.mp4");
        } else if (weatherDescription.toLowerCase().contains("mist") && !currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-Overcast-Night.mp4");
        } else if (weatherDescription.toLowerCase().contains("fog") && !currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-Overcast-Night.mp4");
        } else if (weatherDescription.toLowerCase().contains("sunny") && !currentTimeIsLaterThanSunsetVar) {

            playSeamlessVideo("/Weather-Background-Clear-Night.mp4");
        }
    }

    private void playSeamlessVideo(String videoPath) {
        mediaPlayer = createMediaPlayer(videoPath);
    }

    private boolean currentTimeIsLaterThanSunset() {
        String currentTimeTrimmed = formatDateToDayAndHour(getLocalTime()).split(", ")[1];

        List<String> sunsetAndSunrise = getSunsetAndSunrise();
        String sunsetTimeTrimmed = Objects.requireNonNull(sunsetAndSunrise).get(0);
        String sunriseTimeTrimmed = Objects.requireNonNull(sunsetAndSunrise).get(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

        // Parse the strings into LocalTime objects
        // Compare the two LocalTime objects
        LocalTime currentTime = LocalTime.parse(currentTimeTrimmed, formatter);
        LocalTime sunriseTime = LocalTime.parse(sunriseTimeTrimmed, formatter);
        LocalTime sunsetTime = LocalTime.parse(sunsetTimeTrimmed, formatter);

        // Check if the current time is between sunrise and sunset
        if (currentTime.isAfter(sunriseTime) && currentTime.isBefore(sunsetTime)) {
            return true; // It's daytime
        } else {
            return false; // It's nighttime
        }
    }

    private List<String> getSunsetAndSunrise() {
        List<String> sunsetAndSunrise = new CopyOnWriteArrayList<>();
        Thread thread = new Thread(() -> {
            if (!responseBodiesSecondAPI.containsKey(city)) {
                try {
                    responseBodyGetSunsetSunrise = ForecastAPI.httpResponseForecast(city);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                responseBodyGetSunsetSunrise = responseBodiesSecondAPI.get(city);
            }
            if (responseBodyGetSunsetSunrise != null && !responseBodyGetSunsetSunrise.contains("No matching location found.")) {
                if (!responseBodiesSecondAPI.containsKey(city)) {
                    responseBodiesSecondAPI.put(city, responseBodyGetSunsetSunrise);
                }
                JSONObject response = new JSONObject(responseBodyGetSunsetSunrise);

                JSONArray forecastDays = response.getJSONObject("forecast").getJSONArray("forecastday");

                JSONObject forecast = forecastDays.getJSONObject(0);

                JSONObject astroObject = forecast.getJSONObject("astro");

                sunsetAndSunrise.add(astroObject.getString("sunset"));
                sunsetAndSunrise.add(astroObject.getString("sunrise"));
            }
        });
        thread.start();
        try {
            thread.join(); // Wait for the thread to finish
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return sunsetAndSunrise;
    }
}
