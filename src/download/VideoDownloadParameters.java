package download;


import com.getting.util.executor.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


class VideoDownloadParameters implements Parameters {

    private final File saveDirectory;

    private final String url;

    public VideoDownloadParameters(File saveDirectory, String url) {
        this.saveDirectory = saveDirectory;
        this.url = url;
    }

    @Override
    public List<String> build() {
        List<String> command = new ArrayList<>();
        command.add("-o");
        command.add(saveDirectory.getAbsolutePath());
        command.add(url);
        return command;
    }

}
