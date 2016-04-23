package download;

import executor.Parameters;

import java.util.ArrayList;
import java.util.List;

public class VideoInfoParameters implements Parameters {

    private final String url;

    public VideoInfoParameters(String url) {
        this.url = url;
    }

    @Override
    public List<String> build() {
        List<String> command = new ArrayList<>();
        // something is wrong with "-i"
        command.add("-u");
        command.add(url);
        return command;
    }

}
