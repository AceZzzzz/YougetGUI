package download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LiveStreamDownloadParameter extends VideoDownloadParameter {

    public LiveStreamDownloadParameter(String url, File downloadDirectory) {
        super(url, downloadDirectory);
    }

    @Override
    public List<String> build() {
        List<String> command = new ArrayList<>();
        command.add("-d");
        command.add("-O");
        command.add("" + System.currentTimeMillis() + ".mkv");
        command.add("-o");
        command.add(getDownloadDirectory().getAbsolutePath());
        command.add(getUrl());
        return command;
    }

}
