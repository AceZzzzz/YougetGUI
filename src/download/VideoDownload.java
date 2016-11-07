package download;

import com.getting.util.executor.Executor;
import com.sun.istack.internal.NotNull;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

public class VideoDownload extends Executor {

    private final StringProperty speed = new SimpleStringProperty();
    private VideoDownloadParameter videoDownloadParameter = new VideoDownloadParameter();

    public VideoDownload() {
        super(VideoDownload.class, "you-get-0.4.575-win32.exe");
    }

    public String getSpeed() {
        return speed.get();
    }

    public StringProperty speedProperty() {
        return speed;
    }

    private void updateVideoProfileOnUiThread(@NotNull String profile) {
        Platform.runLater(() -> videoDownloadParameter.setVideoProfile(profile));
    }

    private void updateTitleOnUiThread(@NotNull String title) {
        Platform.runLater(() -> videoDownloadParameter.setTitle(title));
    }

    private void bind(@NotNull VideoDownloadParameter videoDownloadParameter) {
        this.videoDownloadParameter = videoDownloadParameter;
        videoDownloadParameter.setProgress(Double.NEGATIVE_INFINITY);
        videoDownloadParameter.setStatus("");
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

            final String videoProfile = YougetUtil.getVideoProfile(newValue);
            if (videoProfile != null) {
                updateVideoProfileOnUiThread(videoProfile);
            }

            final YougetUtil.DownloadProgress downloadProgress = YougetUtil.getDownloadProgress(newValue);
            if (downloadProgress != null) {
                updateProgressStatusOnUiThread(downloadProgress.description);
                updateProgressOnUiThread(downloadProgress.downloaded / downloadProgress.total);
            }

            updateSpeedOnUiThread(YougetUtil.getSpeed(newValue));
        };
        executorOutputMessage.addListener(listener);

        execute(videoDownloadParameter, false);

        executorOutputMessage.removeListener(listener);
        updateSpeedOnUiThread("");
        updateProgressOnUiThread(1);
    }

    private void updateProgressOnUiThread(double progress) {
        Platform.runLater(() -> videoDownloadParameter.setProgress(progress));
    }

    private void updateProgressStatusOnUiThread(String status) {
        Platform.runLater(() -> videoDownloadParameter.setStatus(status.trim()));
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
