package download;

import com.getting.util.executor.Executor;
import com.sun.istack.internal.NotNull;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.logging.MemoryHandler;
import java.util.regex.Matcher;

public class VideoDownload extends Executor {

    public VideoDownload() {
        super(VideoDownload.class, "you-get-0.4.523-win32.exe");
    }

    private void updateVideoProfileOnUiThread(@NotNull String profile) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                videoProfile.set(profile);
            }

        });
    }

    private void updateTitleOnUiThread(@NotNull String title) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                videoTitle.set(title);
            }

        });
    }

    private void bind(@NotNull VideoDownloadParameter videoDownloadParameter) {
        if (this.videoDownloadParameter != null) {
            this.videoDownloadParameter.progressProperty().unbind();
            this.videoDownloadParameter.statusProperty().unbind();
            this.videoDownloadParameter.titleProperty().unbind();
            this.videoDownloadParameter.videoProfileProperty().unbind();
            this.videoDownloadParameter.speedProperty().unbind();
        }

        videoTitle.set(videoDownloadParameter.getTitle());
        videoProfile.set(videoDownloadParameter.getVideoProfile());
        speed.set("");
        progress.set(Double.NEGATIVE_INFINITY);
        progressStatus.set("");

        this.videoDownloadParameter = videoDownloadParameter;
        videoDownloadParameter.progressProperty().bind(progress);
        videoDownloadParameter.statusProperty().bind(progressStatus);
        videoDownloadParameter.titleProperty().bind(videoTitle);
        videoDownloadParameter.videoProfileProperty().bind(videoProfile);
        videoDownloadParameter.speedProperty().bind(speed);
    }

    private void updateDownloadDataOnUiThread(@NotNull VideoDownloadParameter videoDownloadParameter) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                bind(videoDownloadParameter);
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

    public void download(VideoDownloadParameter videoDownloadParameter, boolean infinite) {
        updateDownloadDataOnUiThread(videoDownloadParameter);

        ChangeListener<String> listener = new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue == null) {
                    return;
                }

                final String title = YougetUtil.getTitle(newValue);
                if (title != null) {
                    updateTitleOnUiThread(title);
                }

                final String videoProfile = YougetUtil.getVideoProfile(newValue);
                if (videoProfile != null) {
                    updateVideoProfileOnUiThread(videoProfile);
                }

                final YougetUtil.DownloadStatus downloadStatus = YougetUtil.getDownloadStatus(newValue);
                if (downloadStatus != null) {
                    updateProgressStatusOnUiThread(downloadStatus.description);
                    updateProgressOnUiThread(downloadStatus.downloaded, downloadStatus.total);
                }

                final String speed = YougetUtil.getSpeed(newValue);
                if (speed != null) {
                    updateSpeedOnUiThread(speed);
                }
            }

        };
        executorOutputMessage.addListener(listener);

        if (infinite) {
            try {
                while (true) {
                    execute(videoDownloadParameter, false);
                    Thread.sleep(RESTART_DOWNLOAD_WAIT_TIME);
                    System.out.println("restart download");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            execute(videoDownloadParameter, false);
        }

        updateSpeedOnUiThread("");
        executorOutputMessage.removeListener(listener);
    }

    private static final long RESTART_DOWNLOAD_WAIT_TIME = 3 * 1000;

    private void updateProgressOnUiThread(double downloadedSize, double totalSize) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                progress.set(downloadedSize / totalSize);
            }

        });
    }

    private void updateProgressStatusOnUiThread(String status) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                VideoDownload.this.progressStatus.set(status.trim());
            }

        });
    }

    private VideoDownloadParameter videoDownloadParameter;

    private final StringProperty videoProfile = new SimpleStringProperty();

    private final StringProperty videoTitle = new SimpleStringProperty();

    private final DoubleProperty progress = new SimpleDoubleProperty();

    private final StringProperty progressStatus = new SimpleStringProperty();

    private final StringProperty speed = new SimpleStringProperty();

}
