package download;

import com.getting.util.executor.Executor;
import com.sun.istack.internal.NotNull;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

public class VideoDownload extends Executor {

    private static final long RESTART_DOWNLOAD_WAIT_TIME = 3 * 1000;
    private final StringProperty videoProfile = new SimpleStringProperty();
    private final StringProperty videoTitle = new SimpleStringProperty();
    private final DoubleProperty progress = new SimpleDoubleProperty();
    private final StringProperty progressStatus = new SimpleStringProperty();
    private final StringProperty speed = new SimpleStringProperty();
    private VideoDownloadParameter videoDownloadParameter;

    public VideoDownload() {
        super(VideoDownload.class, "you-get-0.4.523-win32.exe");
    }

    private void updateVideoProfileOnUiThread(@NotNull String profile) {
        Platform.runLater(() -> videoProfile.set(profile));
    }

    private void updateTitleOnUiThread(@NotNull String title) {
        Platform.runLater(() -> videoTitle.set(title));
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
        Platform.runLater(() -> bind(videoDownloadParameter));
    }

    private void updateSpeedOnUiThread(String speed) {
        Platform.runLater(() -> VideoDownload.this.speed.set(speed));
    }

    public void download(VideoDownloadParameter videoDownloadParameter, boolean infinite) {
        updateDownloadDataOnUiThread(videoDownloadParameter);

        ChangeListener<String> listener = (observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            final String title = YougetUtil.getTitle(newValue);
            if (title != null) {
                updateTitleOnUiThread(title);
            }

            final String videoProfile1 = YougetUtil.getVideoProfile(newValue);
            if (videoProfile1 != null) {
                updateVideoProfileOnUiThread(videoProfile1);
            }

            final YougetUtil.DownloadStatus downloadStatus = YougetUtil.getDownloadStatus(newValue);
            if (downloadStatus != null) {
                updateProgressStatusOnUiThread(downloadStatus.description);
                updateProgressOnUiThread(downloadStatus.downloaded, downloadStatus.total);
            }

            final String speed1 = YougetUtil.getSpeed(newValue);
            if (speed1 != null) {
                updateSpeedOnUiThread(speed1);
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

    private void updateProgressOnUiThread(double downloadedSize, double totalSize) {
        Platform.runLater(() -> progress.set(downloadedSize / totalSize));
    }

    private void updateProgressStatusOnUiThread(String status) {
        Platform.runLater(() -> VideoDownload.this.progressStatus.set(status.trim()));
    }

    @Override
    public void cancel() {
        super.cancel();
        new Thread() {

            @Override
            public void run() {
                forceCancel();
            }

        }.start();
    }
}
