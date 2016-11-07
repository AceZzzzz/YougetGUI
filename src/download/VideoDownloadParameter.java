package download;

import com.getting.util.executor.Parameters;
import javafx.beans.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class VideoDownloadParameter extends Parameters implements Externalizable {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoDownloadParameter.class);

    private final StringProperty videoProfile = new SimpleStringProperty();
    private final StringProperty url = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final DoubleProperty progress = new SimpleDoubleProperty();
    private final ObjectProperty<File> downloadDirectory = new SimpleObjectProperty<>();
    private final StringProperty title = new SimpleStringProperty();

    public VideoDownloadParameter(String url, File downloadDirectory) {
        this.url.set(url);
        this.title.set(url);
        this.downloadDirectory.set(downloadDirectory);
    }

    public VideoDownloadParameter() {
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

    @Override
    public void writeExternal(ObjectOutput out) {
        try {
            out.writeObject(url.get());
            out.writeObject(title.get());
            out.writeObject(downloadDirectory.get());
        } catch (IOException e) {
            LOGGER.error("writeExternal", e);
        }
    }

    @Override
    public void readExternal(ObjectInput in) {
        try {
            url.set((String) in.readObject());
            title.set((String) in.readObject());
            downloadDirectory.set((File) in.readObject());
        } catch (ClassNotFoundException | IOException e) {
            LOGGER.error("readExternal", e);
        }
    }

}
