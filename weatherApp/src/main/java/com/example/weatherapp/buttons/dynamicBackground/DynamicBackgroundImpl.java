package com.example.weatherapp.dynamicBackground;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import weatherApi.ForecastAPI;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.example.weatherapp.Main.formatDateToDayAndHour;
import static com.example.weatherapp.Main.getLocalTime;

public class DynamicBackgroundImpl {
    private StackPane rootLayout;
    private VBox root;
    private final List<Pair<MediaPlayer, Node>> mediaPlayerNodePairs = new ArrayList<>();
    private String lastWeatherDescription;
    private String lastTimeCheck;
    private String city;
    private LinkedHashMap<String, String> responseBodiesSecondAPI;
    private String responseBodyGetSunsetSunrise;
    public DynamicBackgroundImpl(StackPane rootLayout,
                                 VBox root,
                                 String city,
                                 LinkedHashMap<String, String> responseBodiesSecondAPI) {
        this.setRootLayout(rootLayout);
        this.setRoot(root);
        this.lastWeatherDescription = "";
        this.lastTimeCheck = "";
        this.setCity(city);
        this.setResponseBodiesSecondAPI(responseBodiesSecondAPI);
        this.responseBodyGetSunsetSunrise = "";
    }

    public void setRootLayout(StackPane rootLayout) {
        this.rootLayout = rootLayout;
    }

    public void setRoot(VBox root) {
        this.root = root;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setResponseBodiesSecondAPI(LinkedHashMap<String, String> responseBodiesSecondAPI) {
        this.responseBodiesSecondAPI = responseBodiesSecondAPI;
    }
    public void setUpDynamicBackground() {
        Media cloudyNightMedia;
        Media cloudyDayMedia;
        Media overcastDayMedia;
        Media overcastNightMedia;
        Media clearNightMedia;
        Media clearDayMedia;
        Media lightRainDayMedia;
        Media lightRainNightMedia;
        Media heavyRainNightMedia;
        Media heavyRainDayMedia;

        heavyRainDayMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-HeavyRain-Day.mp4")).toString());
        MediaPlayer heavyRainDayMediaPlayer = new MediaPlayer(heavyRainDayMedia);
        MediaView heavyRainDayMediaView = new MediaView(heavyRainDayMediaPlayer);

        heavyRainNightMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-HeavyRain-Night.mp4")).toString());
        MediaPlayer heavyRainNightMediaPlayer = new MediaPlayer(heavyRainNightMedia);
        MediaView heavyRainNightMediaView = new MediaView(heavyRainNightMediaPlayer);

        lightRainNightMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-LightRain-Night.mp4")).toString());
        MediaPlayer lightRainNightMediaPlayer = new MediaPlayer(lightRainNightMedia);
        MediaView lightRainNightMediaView = new MediaView(lightRainNightMediaPlayer);

        lightRainDayMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-LightRain-Day.mp4")).toString());
        MediaPlayer lightRainDayMediaPlayer = new MediaPlayer(lightRainDayMedia);
        MediaView lightRainDayMediaView = new MediaView(lightRainDayMediaPlayer);

        cloudyNightMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-Cloudy-Night.mp4")).toString());
        MediaPlayer cloudyNightPlayer = new MediaPlayer(cloudyNightMedia);
        cloudyNightPlayer.setMute(true);
        MediaView cloudyNightMediaView = new MediaView(cloudyNightPlayer);

        cloudyDayMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-Cloudy-Day.mp4")).toString());
        MediaPlayer cloudyDayMediaPlayer = new MediaPlayer(cloudyDayMedia);
        MediaView cloudyDayMediaView = new MediaView(cloudyDayMediaPlayer);

        overcastDayMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-Overcast-Day.mp4")).toString());
        MediaPlayer overcastDayPlayer = new MediaPlayer(overcastDayMedia);
        overcastDayPlayer.setMute(true);
        MediaView overcastDayMediaView = new MediaView(overcastDayPlayer);

        overcastNightMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-Overcast-Night.mp4")).toString());
        MediaPlayer overcastNightPlayer = new MediaPlayer(overcastNightMedia);
        MediaView overcastNightMediaView = new MediaView(overcastNightPlayer);

        clearNightMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-Clear-Night.mp4")).toString());
        MediaPlayer clearNightPlayer = new MediaPlayer(clearNightMedia);
        MediaView clearNightMediaView = new MediaView(clearNightPlayer);

        clearDayMedia = new Media(Objects.requireNonNull(getClass().getResource("/Weather-Background-Clear-Day.mp4")).toString());
        MediaPlayer clearDayPlayer = new MediaPlayer(clearDayMedia);
        MediaView clearDayMediaView = new MediaView(clearDayPlayer);

        Objects.requireNonNull(rootLayout).getChildren().addAll(//
                lightRainDayMediaView, //
                cloudyNightMediaView, //
                overcastDayMediaView, //
                clearNightMediaView, //
                clearDayMediaView, //
                lightRainNightMediaView, //
                heavyRainDayMediaView, //
                heavyRainNightMediaView, //
                cloudyDayMediaView, //
                overcastNightMediaView, //
                root //
        );
        mediaPlayerNodePairs.add(new Pair<>(lightRainDayMediaPlayer, rootLayout.getChildren().get(0)));
        mediaPlayerNodePairs.add(new Pair<>(cloudyNightPlayer, rootLayout.getChildren().get(1)));
        mediaPlayerNodePairs.add(new Pair<>(overcastDayPlayer, rootLayout.getChildren().get(2)));
        mediaPlayerNodePairs.add(new Pair<>(clearNightPlayer, rootLayout.getChildren().get(3)));
        mediaPlayerNodePairs.add(new Pair<>(clearDayPlayer, rootLayout.getChildren().get(4)));
        mediaPlayerNodePairs.add(new Pair<>(lightRainNightMediaPlayer, rootLayout.getChildren().get(5)));
        mediaPlayerNodePairs.add(new Pair<>(heavyRainDayMediaPlayer, rootLayout.getChildren().get(6)));
        mediaPlayerNodePairs.add(new Pair<>(heavyRainNightMediaPlayer, rootLayout.getChildren().get(7)));
        mediaPlayerNodePairs.add(new Pair<>(cloudyDayMediaPlayer, rootLayout.getChildren().get(8)));
        mediaPlayerNodePairs.add(new Pair<>(overcastNightPlayer, rootLayout.getChildren().get(9)));

        mediaPlayerNodePairs.get(0).getKey().setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayerNodePairs.get(1).getKey().setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayerNodePairs.get(2).getKey().setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayerNodePairs.get(3).getKey().setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayerNodePairs.get(4).getKey().setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayerNodePairs.get(5).getKey().setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayerNodePairs.get(6).getKey().setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayerNodePairs.get(7).getKey().setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayerNodePairs.get(8).getKey().setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayerNodePairs.get(9).getKey().setCycleCount(MediaPlayer.INDEFINITE);
    }
    public void stopAllMediaPlayersAndHideAllNodes() {
        for (Pair<MediaPlayer, Node> pair : mediaPlayerNodePairs) {
            if (pair.getKey().getStatus() == MediaPlayer.Status.PLAYING &&
                    pair.getValue().isVisible()) {
                pair.getKey().seek(Duration.ZERO);
                pair.getKey().stop();
                pair.getValue().setVisible(false);
            } else if (pair.getKey().getStatus() == MediaPlayer.Status.READY &&
                    pair.getValue().isVisible()){
                pair.getKey().seek(Duration.ZERO);
                pair.getKey().stop();
                pair.getValue().setVisible(false);
            }
        }
    }
    public void switchVideoBackground(String weatherDescription) {
        boolean currentTimeIsLaterThanSunsetVar = currentTimeIsLaterThanSunset();
        if (!lastWeatherDescription.equals(weatherDescription)) {
            stopAllMediaPlayersAndHideAllNodes();
            playDesiredVideo(weatherDescription, currentTimeIsLaterThanSunsetVar);
        } else {
            if (!currentTimeIsLaterThanSunsetVar && lastTimeCheck.equals("Day")) {
                stopAllMediaPlayersAndHideAllNodes();
                playDesiredVideo(weatherDescription, false);
            } else if (currentTimeIsLaterThanSunsetVar && lastTimeCheck.equals("Night")) {
                stopAllMediaPlayersAndHideAllNodes();
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
        if (weatherDescription.toLowerCase().contains("light rain") &&
                currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(0).getKey().play();
            mediaPlayerNodePairs.get(0).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("cloud") &&
                !currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(1).getKey().play();
            mediaPlayerNodePairs.get(1).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("overcast") &&
                currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(2).getKey().play();
            mediaPlayerNodePairs.get(2).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("clear") &&
                !currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(3).getKey().play();
            mediaPlayerNodePairs.get(3).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("clear") &&
                currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(4).getKey().play();
            mediaPlayerNodePairs.get(4).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("light rain") &&
                !currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(5).getKey().play();
            mediaPlayerNodePairs.get(5).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("heavy rain") &&
                currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(6).getKey().play();
            mediaPlayerNodePairs.get(6).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("heavy rain") &&
                !currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(7).getKey().play();
            mediaPlayerNodePairs.get(7).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("sunny") && currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(4).getKey().play();
            mediaPlayerNodePairs.get(4).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("rain") &&
                currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(6).getKey().play();
            mediaPlayerNodePairs.get(6).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("rain") &&
                !currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(7).getKey().play();
            mediaPlayerNodePairs.get(7).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("overcast") &&
                !currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(9).getKey().play();
            mediaPlayerNodePairs.get(9).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("cloud") &&
                currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(8).getKey().play();
            mediaPlayerNodePairs.get(8).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("mist") &&
                currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(8).getKey().play();
            mediaPlayerNodePairs.get(8).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("fog") &&
                currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(8).getKey().play();
            mediaPlayerNodePairs.get(8).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("mist") &&
                !currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(1).getKey().play();
            mediaPlayerNodePairs.get(1).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("fog") &&
                !currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(1).getKey().play();
            mediaPlayerNodePairs.get(1).getValue().setVisible(true);
        } else if (weatherDescription.toLowerCase().contains("sunny") && !currentTimeIsLaterThanSunsetVar) {

            mediaPlayerNodePairs.get(4).getKey().play();
            mediaPlayerNodePairs.get(4).getValue().setVisible(true);
        }
    }
    private boolean currentTimeIsLaterThanSunset() {
        String currentTimeTrimmed = formatDateToDayAndHour(getLocalTime(city)).split(", ")[1];

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
