package download;

import com.sun.istack.internal.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YougetUtil {

    private static final Pattern VIDEO_PROFILE_REGEX = Pattern.compile(".+video-profile:(?<videoprofile>.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PROGRESS_REGEX = Pattern.compile("(?<status>(?<downloaded>.+)/(?<total>.+)MB)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TITLE_REGEX = Pattern.compile(".*(title|playlist):(?<title>.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SPEED_REGEX = Pattern.compile(".+ (?<speed>\\d+ (kB|MB)/s)$", Pattern.CASE_INSENSITIVE);

    public static String getTitle(@NotNull String message) {
        Matcher matcher = YougetUtil.TITLE_REGEX.matcher(message);
        if (matcher.matches()) {
            return matcher.group("title").trim();
        }

        return null;
    }

    public static String getVideoProfile(String message) {
        Matcher matcher = YougetUtil.VIDEO_PROFILE_REGEX.matcher(message);
        if (matcher.matches()) {
            return matcher.group("videoprofile").trim();
        }

        return null;
    }

    public static String getSpeed(String message) {
        Matcher matcher = YougetUtil.SPEED_REGEX.matcher(message);
        if (matcher.matches()) {
            return matcher.group("speed");
        }

        return null;
    }

    public static DownloadStatus getDownloadStatus(String message) {
        for (String split : message.split("[()]")) {
            Matcher matcher = YougetUtil.PROGRESS_REGEX.matcher(split);
            if (matcher.matches()) {
                return new DownloadStatus(matcher.group("status"), Double.parseDouble(matcher.group("downloaded")), Double.parseDouble(matcher.group("total")));
            }
        }

        return null;
    }

    public static class DownloadStatus {

        public final String description;
        public final double downloaded;
        public final double total;

        public DownloadStatus(String description, double downloaded, double total) {
            this.description = description;
            this.downloaded = downloaded;
            this.total = total;
        }

    }

}
