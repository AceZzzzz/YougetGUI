package download;

import com.getting.util.executor.Executor;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jetbrains.annotations.NotNull;

public class VideoDownload extends Executor {

    private final StringProperty downloadSpeed = new SimpleStringProperty();
    @NotNull
    private VideoDownloadTask videoDownloadTask = new VideoDownloadTask();

    public VideoDownload() {
        super(VideoDownload.class, "you-get-0.4.626-win32.exe");
        executorOutputMessage.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            final String title = YougetUtil.getTitle(newValue);
            if (title != null) {
                Platform.runLater(() -> videoDownloadTask.setTitle(title));
            }

            final String videoProfile = YougetUtil.getVideoProfile(newValue);
            if (videoProfile != null) {
                Platform.runLater(() -> videoDownloadTask.setVideoProfile(videoProfile));
            }

            final YougetUtil.DownloadProgress downloadProgress = YougetUtil.getDownloadProgress(newValue);
            if (downloadProgress != null) {
                Platform.runLater(() -> {
                    videoDownloadTask.setStatus(downloadProgress.description.trim());
                    videoDownloadTask.setProgress(downloadProgress.downloaded / downloadProgress.total);
                });
            }

            Platform.runLater(() -> downloadSpeed.set(YougetUtil.getSpeed(newValue)));
        });
    }

    public String getDownloadSpeed() {
        return downloadSpeed.get();
    }

    @NotNull
    public StringProperty downloadSpeedProperty() {
        return downloadSpeed;
    }

    public void download(@NotNull VideoDownloadTask task) {
        Platform.runLater(() -> {
            this.videoDownloadTask = task;
            task.setProgress(Double.NEGATIVE_INFINITY);
            task.setStatus("");
        });

        execute(task, false);

        Platform.runLater(() -> {
            downloadSpeed.set("");
            task.setProgress(1);
        });
    }

    @Override
    public void cancel() {
        super.cancel();
        forceCancel();
    }

}
