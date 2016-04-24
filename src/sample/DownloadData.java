package sample;

import javafx.beans.property.*;

import java.io.File;


public class DownloadData {

    private final StringProperty videoProfile = new SimpleStringProperty();

    private final StringProperty url = new SimpleStringProperty();

    private final StringProperty status = new SimpleStringProperty();

    private final DoubleProperty progress = new SimpleDoubleProperty();

    private final ObjectProperty<File> downloadDirectory = new SimpleObjectProperty<>();

    private final StringProperty name = new SimpleStringProperty();

    public DownloadData(String url, File downloadDirectory) {
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

    public void setName(String name) {
        this.name.set(name);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public File getDownloadDir() {
        return downloadDirectory.get();
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
