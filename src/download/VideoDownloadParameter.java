package download;

import com.getting.util.executor.Parameters;
import javafx.beans.property.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class VideoDownloadParameter implements Parameters {

    @Override
    public List<String> build() {
        List<String> command = new ArrayList<>();
        command.add("-o");
        command.add(downloadDirectory.get().getAbsolutePath());
        command.add(url.get());
        return command;
    }

    private final StringProperty videoProfile = new SimpleStringProperty();

    private final StringProperty url = new SimpleStringProperty();

    private final StringProperty status = new SimpleStringProperty();

    private final DoubleProperty progress = new SimpleDoubleProperty();

    private final ObjectProperty<File> downloadDirectory = new SimpleObjectProperty<>();

    private final StringProperty title = new SimpleStringProperty();

    private final StringProperty speed = new SimpleStringProperty();

    public String getSpeed() {
        return speed.get();
    }

    public StringProperty speedProperty() {
        return speed;
    }

    public VideoDownloadParameter(String url, File downloadDirectory) {
        this.url.set(url);
        this.downloadDirectory.set(downloadDirectory);
    }

    public String getVideoProfile() {
        return videoProfile.get();
    }

    public double getProgress() {
        return progress.get();
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    public void setVideoProfile(String videoProfile) {
        this.videoProfile.set(videoProfile);
    }

    public StringProperty videoProfileProperty() {
        return videoProfile;
    }

    public File getDownloadDirectory() {
        return downloadDirectory.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public ObjectProperty<File> downloadDirectoryProperty() {
        return downloadDirectory;
    }

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

}
