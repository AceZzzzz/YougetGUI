package download;

import com.getting.util.executor.Executor;
import com.sun.istack.internal.NotNull;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoDownload extends Executor {

    public VideoDownload() {
        super(VideoDownload.class, "you-get-0.4.455-win32.exe");
    }

    private void updateVideoProfileOnUiThread(String profile) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                videoProfile.set(profile);
            }

        });
    }

    private void updateTitleOnUiThread(String title) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                videoTitle.set(title);
            }

        });
    }

    private void bind(@NotNull DownloadData downloadData) {
        if (this.downloadData != null) {
            this.downloadData.progressProperty().unbind();
            this.downloadData.statusProperty().unbind();
            this.downloadData.titleProperty().unbind();
            this.downloadData.videoProfileProperty().unbind();
            this.downloadData.speedProperty().unbind();
        }

        videoTitle.set(downloadData.getTitle());
        videoProfile.set(downloadData.getVideoProfile());
        downloadedSize.set(0);
        totalSize.set(0);
        speed.set("");

        this.downloadData = downloadData;
        downloadData.progressProperty().bind(progress);
        downloadData.statusProperty().bind(progressStatus);
        downloadData.titleProperty().bind(videoTitle);
        downloadData.videoProfileProperty().bind(videoProfile);
        downloadData.speedProperty().bind(speed);
    }

    private void updateDownloadDataOnUiThread(@NotNull DownloadData downloadData) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                bind(downloadData);
            }

        });
    }

    private void updateSpeedOnUiThread(String speed) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                VideoDownload.this.speed.set(speed);
            }

        });
    }

    public void download(DownloadData downloadData) {
        updateDownloadDataOnUiThread(downloadData);

        if (!downloadData.getDownloadDirectory().exists()) {
            return;
        }

        ChangeListener<String> listener = new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                {
                    Matcher matcher = TITLE_REGEX.matcher(newValue);
                    if (matcher.matches()) {
                        updateTitleOnUiThread(matcher.group("title").trim());
                    }
                }

                {
                    Matcher matcher = VIDEO_PROFILE_REGEX.matcher(newValue);
                    if (matcher.matches()) {
                        updateVideoProfileOnUiThread(matcher.group("videoprofile").trim());
                    }
                }

                for (String split : newValue.split("[()]")) {
                    Matcher matcher = PROGRESS_REGEX.matcher(split);
                    if (matcher.matches()) {
                        updateProgressOnUiThread(Double.parseDouble(matcher.group("downloaded")), Double.parseDouble(matcher.group("total")));
                    }
                }

                {
                    Matcher matcher = SPEED_REGEX.matcher(newValue);
                    if (matcher.matches()) {
                        updateSpeedOnUiThread(matcher.group("speed"));
                    }
                }
            }
        };
        executorOutputMessage.addListener(listener);
        updateProgressOnUiThread(0, 0);
        updateSpeedOnUiThread("");
        execute(new VideoDownloadParameters(downloadData.getDownloadDirectory(), downloadData.getUrl()), false);
        updateSpeedOnUiThread("");
        executorOutputMessage.removeListener(listener);
    }

    private void updateProgressOnUiThread(double downloadedSize, double totalSize) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                VideoDownload.this.downloadedSize.set(downloadedSize);
                VideoDownload.this.totalSize.set(totalSize);
            }

        });
    }

    private DownloadData downloadData;

    private final DoubleProperty downloadedSize = new SimpleDoubleProperty();

    private final DoubleProperty totalSize = new SimpleDoubleProperty();

    private final StringProperty videoProfile = new SimpleStringProperty();

    private final StringProperty videoTitle = new SimpleStringProperty();

    private final ObservableDoubleValue progress = downloadedSize.divide(totalSize);

    private final ObservableStringValue progressStatus = downloadedSize.asString().concat("/").concat(totalSize.asString()).concat(" MB");

    private static final Pattern VIDEO_PROFILE_REGEX = Pattern.compile(".+video-profile:(?<videoprofile>.+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern PROGRESS_REGEX = Pattern.compile("(?<downloaded>[\\d\\. ]+)/(?<total>[\\d\\. ]+)MB", Pattern.CASE_INSENSITIVE);

    private static final Pattern TITLE_REGEX = Pattern.compile(".*(title|playlist):(?<title>.+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern SPEED_REGEX = Pattern.compile(".+ (?<speed>\\d+ (kB|MB)/s)$", Pattern.CASE_INSENSITIVE);

    private final StringProperty speed = new SimpleStringProperty();

}
