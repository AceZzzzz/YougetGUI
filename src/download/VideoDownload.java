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

    public void download(VideoDownloadParameter videoDownloadParameter) {
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

            final YougetUtil.DownloadProgress downloadProgress = YougetUtil.getDownloadProgress(newValue);
            if (downloadProgress != null) {
                updateProgressStatusOnUiThread(downloadProgress.description);
                updateProgressOnUiThread(downloadProgress.downloaded / downloadProgress.total);
            }

            final String speed = YougetUtil.getSpeed(newValue);
            if (speed != null) {
                updateSpeedOnUiThread(speed);
            }
        };
        executorOutputMessage.addListener(listener);

        execute(videoDownloadParameter, false);

        executorOutputMessage.removeListener(listener);
        updateSpeedOnUiThread("");
        if (progress.get() == Double.NEGATIVE_INFINITY) {
            updateProgressOnUiThread(0);
        }
    }

    private void updateProgressOnUiThread(double progress) {
        Platform.runLater(() -> this.progress.set(progress));
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
