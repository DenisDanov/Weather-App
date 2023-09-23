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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

import static com.example.weatherapp.Main.formatDateToDayAndHour;
import static com.example.weatherapp.Main.getLocalTime;

public class DynamicBackgroundImpl {

    private String lastWeatherDescription;
    private String lastTimeCheck;
    private String city;
    private ConcurrentHashMap<String, String> responseBodiesSecondAPI;
    private String responseBodyGetSunsetSunrise;
    private final MediaView mediaView = new MediaView();
    private Stage stage;
    private Scene mainScene;
    private final Map<String, String> videoPaths;
    private final FadeTransition fadeOut;
    private final FadeTransition fadeIn;

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
        this.videoPaths = new HashMap<>();
        this.fadeOut = new FadeTransition(Duration.millis(100), mediaView);
        this.fadeIn = new FadeTransition(Duration.millis(100), mediaView);
        fadeIn.setFromValue(1);
        fadeOut.setFromValue(1);
        fadeIn.setToValue(1);
        fadeOut.setToValue(1);

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

    public void addVideosPaths() {
        videoPaths.put("lightrain Day", "Weather-Background-LightRain-Day.mp4");
        videoPaths.put("lightrain Night", "Weather-Background-LightRain-Night.mp4");
        videoPaths.put("lightdrizzle Day", "Weather-Background-LightRain-Day.mp4");
        videoPaths.put("lightdrizzle Night", "Weather-Background-LightRain-Night.mp4");
        videoPaths.put("cloud Day", "Weather-Background-Cloudy-Day.mp4");
        videoPaths.put("cloud Night", "Weather-Background-Cloudy-Night.mp4");
        videoPaths.put("overcast Day", "Weather-Background-Overcast-Day.mp4");
        videoPaths.put("overcast Night", "Weather-Background-Overcast-Night.mp4");
        videoPaths.put("clear Day", "Weather-Background-Clear-Day.mp4");
        videoPaths.put("clear Night", "Weather-Background-Clear-Night.mp4");
        videoPaths.put("sunny Day", "Weather-Background-Clear-Day.mp4");
        videoPaths.put("sunny Night", "Weather-Background-Clear-Night.mp4");
        videoPaths.put("rain Day", "Weather-Background-HeavyRain-Day.mp4");
        videoPaths.put("rain Night", "Weather-Background-HeavyRain-Night.mp4");
        videoPaths.put("mist Day", "Weather-Background-Overcast-Day.mp4");
        videoPaths.put("mist Night", "Weather-Background-Overcast-Night.mp4");
        videoPaths.put("fog Day", "Weather-Background-Overcast-Day.mp4");
        videoPaths.put("fog Night", "Weather-Background-Overcast-Night.mp4");
    }

    private MediaPlayer loadMediaPlayerInBackground(String resourcePath) {
        ExecutorService executorService = Executors.newCachedThreadPool();

        CompletableFuture<MediaPlayer> futureMediaPlayer = CompletableFuture.supplyAsync(() -> {
            return createAndLoadMediaPlayer(resourcePath);
        }, executorService);

        try {
            executorService.shutdown();
            return futureMediaPlayer.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(System.out);
            executorService.shutdown();
            return null;
        }
    }

    private MediaPlayer createAndLoadMediaPlayer(String resourcePath) {
        MediaPlayer mediaPlayer = new MediaPlayer(new Media(Objects.requireNonNull
                (getClass().getResource("/" + resourcePath)).toString()));
        mediaPlayer.setAutoPlay(false); // Prevent immediate playback
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.setMute(true);
        return mediaPlayer;
    }

    private void createMediaPlayerAndPlayIt(String resourcePath) {

        if (mediaView.getMediaPlayer() != null) {
            // Create a new MediaPlayer for the second video
            MediaPlayer newMediaPlayer = loadMediaPlayerInBackground(resourcePath);

            // Set an event handler for when the fade out animation is finished
            fadeOut.setOnFinished(event -> {
                // Crossfade to the second video
                disposeMediaPlayerAsync(mediaView);
                fadeInToNewVideo(newMediaPlayer, fadeIn);
            });

            if (stage.getScene() != mainScene) {
                fadeOut.setDuration(Duration.millis(1));
                fadeIn.setDuration(Duration.millis(1));
            } else if (fadeOut.getDuration() == Duration.millis(1)) {
                fadeOut.setDuration(Duration.millis(100));
                fadeIn.setDuration(Duration.millis(100));
            }
            // Start the fade-out animation
            fadeOut.play();
        } else {
            MediaPlayer mediaPlayer = loadMediaPlayerInBackground(resourcePath);

            mediaView.setMediaPlayer(mediaPlayer);
            mediaView.setSmooth(true);
            mediaView.setVisible(true);

            Objects.requireNonNull(mediaPlayer).play();
        }
    }

    public static void disposeMediaPlayerAsync(MediaView mediaView) {
        if (mediaView == null || mediaView.getMediaPlayer() == null) {
            return;
        }

        MediaPlayer mediaPlayer = mediaView.getMediaPlayer();

        // Create a background thread for disposing the MediaPlayer
        Thread disposeThread = new Thread(() -> {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        });

        // Start and run the thread
        disposeThread.start();

        // stop the thread
        try {
            disposeThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void fadeInToNewVideo(MediaPlayer newMediaPlayer, FadeTransition fadeIn) {
        mediaView.setMediaPlayer(newMediaPlayer);

        fadeIn.setNode(mediaView);

        // Start the fade-in animation
        newMediaPlayer.play();
        fadeIn.play();
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
        String weatherDescriptionRefactor = weatherDescription.toLowerCase().replaceAll("\\s", "");
        String booleanConvert;
        if (!currentTimeIsLaterThanSunsetVar) {
            booleanConvert = "Night";
        } else {
            booleanConvert = "Day";
        }
        String finalBooleanConvert = booleanConvert;
        String videoPath = (videoPaths.entrySet().
                stream()
                .filter(entry -> weatherDescriptionRefactor.contains(entry.getKey().split(" ")[0]) &&
                        finalBooleanConvert.equals(entry.getKey().split(" ")[1]))
                .map(Map.Entry::getValue)
                .findFirst()).toString();
        createMediaPlayerAndPlayIt(videoPath.substring(9, videoPath.length() - 1));
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
                    responseBodyGetSunsetSunrise = ForecastAPI.httpResponseDailyForecast(city);
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
