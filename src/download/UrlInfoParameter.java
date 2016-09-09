package download;

import com.getting.util.executor.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UrlInfoParameter implements Parameters {

    private final String url;

    public UrlInfoParameter(String url) {
        this.url = url;
    }

    @Override
    public List<String> build() {
        List<String> command = new ArrayList<>();
        command.add("-d");
        command.add("-i");
        command.add(url);
        return command;
    }

    @Override
    public File getOutputDirectory() {
        return null;
    }

}
