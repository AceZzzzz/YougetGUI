package download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LiveStreamDownloadParameter extends VideoDownloadParameter {

    @Override
    public List<String> build() {
        List<String> command = new ArrayList<>();
        command.add("-O");
        command.add("" + System.currentTimeMillis() + ".mkv");
        command.add("-o");
        command.add(downloadDirectory.get().getAbsolutePath());
        command.add(url.get());
        return command;
    }

    public LiveStreamDownloadParameter(String url, File downloadDirectory) {
        super(url, downloadDirectory);
    }

}
