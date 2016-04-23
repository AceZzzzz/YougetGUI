package sample;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;


public class DownloadData {

    private StringProperty url = new SimpleStringProperty();

    private StringProperty progress = new SimpleStringProperty();

    private ObjectProperty<File> saveDir = new SimpleObjectProperty<>();

    private StringProperty name = new SimpleStringProperty();

    public DownloadData(String url, File saveDir) {
        this.url.set(url);
        this.saveDir.set(saveDir);
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

    public File getSaveDir() {
        return saveDir.get();
    }

    public ObjectProperty<File> saveDirProperty() {
        return saveDir;
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
