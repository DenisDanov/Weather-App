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
import parsingWeatherData.WeatherData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.weatherapp.Main.formatDateToDayAndHour;
import static com.example.weatherapp.Main.formatHour;

public class DynamicBackgroundImpl {

    private String lastWeatherDescription;
    private String lastTimeCheck;
    private final MediaView mediaView = new MediaView();
    private Stage stage;
    private Scene mainScene;
    private final Map<String, String> videoPaths;
    private final FadeTransition fadeOut;
    private final FadeTransition fadeIn;
    private WeatherData weatherData;
    private final ExecutorService mediaPlayerLoader = Executors.newCachedThreadPool();

    public DynamicBackgroundImpl(StackPane rootLayout,
                                 VBox root,
                                 Stage stage,
                                 Scene mainScene,
                                 WeatherData weatherData) {
        this.lastWeatherDescription = "";
        this.lastTimeCheck = "";
        this.setStage(stage);
        this.setMainScene(mainScene);
        this.videoPaths = new HashMap<>();
        this.fadeOut = new FadeTransition(Duration.millis(100), mediaView);
        this.fadeIn = new FadeTransition(Duration.millis(100), mediaView);
        this.setForecastData(weatherData);

        fadeIn.setFromValue(1);
        fadeOut.setFromValue(1);
        fadeIn.setToValue(1);
        fadeOut.setToValue(1);

        Objects.requireNonNull(rootLayout).getChildren().addAll(mediaView, root);
    }

    public void setForecastData(WeatherData weatherData) {
        this.weatherData = weatherData;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setMainScene(Scene mainScene) {
        this.mainScene = mainScene;
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
        CompletableFuture<MediaPlayer> futureMediaPlayer = CompletableFuture.supplyAsync(() ->
                createAndLoadMediaPlayer(resourcePath), mediaPlayerLoader);

        try {
            return futureMediaPlayer.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(System.out);
            return null;
        }
    }

    private MediaPlayer createAndLoadMediaPlayer(String resourcePath) {
        return new MediaPlayer(new Media(Objects.requireNonNull
                (getClass().getResource("/" + resourcePath)).toString()));
    }

    private void createMediaPlayerAndPlayIt(String resourcePath) {

        if (mediaView.getMediaPlayer() != null) {
            // Create a new MediaPlayer for the second video
            MediaPlayer newMediaPlayer = loadMediaPlayerInBackground(resourcePath);
            Objects.requireNonNull(newMediaPlayer).setAutoPlay(false); // Prevent immediate playback
            newMediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            newMediaPlayer.setMute(true);

            // Set an event handler for when the fade out animation is finished
            fadeOut.setOnFinished(event -> {
                // Crossfade to the second video
                mediaView.getMediaPlayer().stop();
                mediaView.getMediaPlayer().dispose();
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
            Objects.requireNonNull(mediaPlayer).setAutoPlay(false); // Prevent immediate playback
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setMute(true);

            mediaView.setMediaPlayer(mediaPlayer);
            mediaView.setSmooth(true);
            mediaView.setVisible(true);

            Objects.requireNonNull(mediaPlayer).play();
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

        if (booleanConvert.equals("Day")) {
            if (mainScene.getStylesheets().size() == 2) {
                mainScene.getStylesheets().remove(1);
            }
        } else {
            if (mainScene.getStylesheets().size() != 2) {
                mainScene.getStylesheets().add("mainPageNight.css");
            }
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
        String currentTimeTrimmed = formatDateToDayAndHour(weatherData.getLocation().getLocaltime()).split(", ")[1];
        String sunsetTimeTrimmed = formatHour(weatherData.getForecast().getForecastday().get(0).getAstro().getSunset());
        String sunriseTimeTrimmed = formatHour(weatherData.getForecast().getForecastday().get(0).getAstro().getSunrise());

        SimpleDateFormat inputFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        try {
            currentTimeTrimmed = currentTimeTrimmed.replace("пр.об.", "AM").replace("сл.об.", "PM");
            sunriseTimeTrimmed = sunriseTimeTrimmed.replace("пр.об.", "AM").replace("сл.об.", "PM");
            sunsetTimeTrimmed = sunsetTimeTrimmed.replace("пр.об.", "AM").replace("сл.об.", "PM");

            Date currentTimeDate = inputFormat.parse(currentTimeTrimmed);
            Date sunriseTimeDate = inputFormat.parse(sunriseTimeTrimmed);
            Date sunsetTimeDate = inputFormat.parse(sunsetTimeTrimmed);

            // Convert the parsed times to LocalTime
            LocalTime currentTime = currentTimeDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            LocalTime sunriseTime = sunriseTimeDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            LocalTime sunsetTime = sunsetTimeDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            if (currentTime.isAfter(sunriseTime) && currentTime.isBefore(sunsetTime)) {
                return true; // It's daytime
            } else {
                return false; // It's nighttime
            }
        } catch (ParseException e) {
            e.printStackTrace(System.out);
            return false; // Handle as nighttime or an error state
        }
    }

}
