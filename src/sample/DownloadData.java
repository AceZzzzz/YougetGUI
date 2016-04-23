package sample;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;


public class DownloadData {

    private final StringProperty url = new SimpleStringProperty();

    private final StringProperty progress = new SimpleStringProperty("等待下载...");

    private final ObjectProperty<File> downloadDirectory = new SimpleObjectProperty<>();

    private final StringProperty name = new SimpleStringProperty();

    public DownloadData(String url, File downloadDirectory) {
        this.url.set(url);
        this.downloadDirectory.set(downloadDirectory);
    }

    public void setProgress(String progress) {
        this.progress.set(progress);
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

    public String getProgress() {
        return progress.get();
    }

    public StringProperty progressProperty() {
        return progress;
    }

}
