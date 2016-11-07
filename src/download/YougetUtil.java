package download;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YougetUtil {

    private static final Pattern VIDEO_PROFILE_REGEX = Pattern.compile(".+video-profile:(?<videoprofile>.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PROGRESS_REGEX = Pattern.compile("(?<status>(?<downloaded>.+)/(?<total>.+)MB)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TITLE_REGEX = Pattern.compile(".*(title|playlist):(?<title>.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SPEED_REGEX = Pattern.compile(".+ (?<downloadSpeed>\\d+ (kB|MB)/s)$", Pattern.CASE_INSENSITIVE);

    @Nullable
    public static String getTitle(@NotNull String message) {
        Matcher matcher = YougetUtil.TITLE_REGEX.matcher(message);
        if (matcher.matches()) {
            return matcher.group("title").trim();
        }

        return null;
    }

    @Nullable
    public static String getVideoProfile(@NotNull String message) {
        Matcher matcher = YougetUtil.VIDEO_PROFILE_REGEX.matcher(message);
        if (matcher.matches()) {
            return matcher.group("videoprofile").trim();
        }

        return null;
    }

    @Nullable
    public static String getSpeed(@NotNull String message) {
        Matcher matcher = YougetUtil.SPEED_REGEX.matcher(message);
        if (matcher.matches()) {
            return matcher.group("downloadSpeed");
        }

        return null;
    }

    @Nullable
    public static DownloadProgress getDownloadProgress(@NotNull String message) {
        for (String split : message.split("[()]")) {
            Matcher matcher = YougetUtil.PROGRESS_REGEX.matcher(split);
            if (matcher.matches()) {
                return new DownloadProgress(matcher.group("status"), Double.parseDouble(matcher.group("downloaded")), Double.parseDouble(matcher.group("total")));
            }
        }

        return null;
    }

    public static class DownloadProgress {

        public final String description;
        public final double downloaded;
        public final double total;

        public DownloadProgress(String description, double downloaded, double total) {
            this.description = description;
            this.downloaded = downloaded;
            this.total = total;
        }

    }

}
