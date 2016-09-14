package download;

import com.getting.util.executor.Parameters;
import javafx.beans.property.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class VideoDownloadParameter implements Parameters {

    private final StringProperty videoProfile = new SimpleStringProperty();
    private final StringProperty url = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final DoubleProperty progress = new SimpleDoubleProperty();
    private final ObjectProperty<File> downloadDirectory = new SimpleObjectProperty<>();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty speed = new SimpleStringProperty();

    public VideoDownloadParameter(String url, File downloadDirectory) {
        this.url.set(url);
        this.downloadDirectory.set(downloadDirectory);
    }

    @Override
    public List<String> build() {
        List<String> command = new ArrayList<>();
        command.add("-d");
        command.add("-o");
        command.add(downloadDirectory.get().getAbsolutePath());
        command.add(url.get());
        return command;
    }

    @Override
    public File getOutputDirectory() {
        return downloadDirectory.get();
    }

    public String getSpeed() {
        return speed.get();
    }

    public StringProperty speedProperty() {
        return speed;
    }

    public String getVideoProfile() {
        return videoProfile.get();
    }

    public void setVideoProfile(String videoProfile) {
        this.videoProfile.set(videoProfile);
    }

    public double getProgress() {
        return progress.get();
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public StringProperty videoProfileProperty() {
        return videoProfile;
    }

    public File getDownloadDirectory() {
        return downloadDirectory.get();
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
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

    public void setStatus(String status) {
        this.status.set(status);
    }

    public StringProperty statusProperty() {
        return status;
    }

}
