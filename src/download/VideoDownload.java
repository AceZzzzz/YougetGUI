package download;

import debug.Debug;
import executor.Executor;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import sample.DownloadData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoDownload extends Executor {

    public VideoDownload() {
        super(VideoDownload.class, "you-get-0.4.365-win32.exe");
        new ProgressChecker().start();
    }

    public void updateVideoInfo(DownloadData downloadData) {
        updateDownloadData(downloadData);

        boolean[] findTitle = new boolean[]{false};
        ChangeListener<String> listener = new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (Debug.LOG) {
                    System.out.println(newValue);
                }

                // just get first title
                if (!findTitle[0]) {
                    Matcher matcher = TITLE_REGEX.matcher(newValue);
                    if (matcher.matches()) {
                        findTitle[0] = true;
                        updateTitleOnUiThread(matcher.group("title").trim());
                    }
                }

                {
                    Matcher matcher = VIDEO_PROFILE_REGEX.matcher(newValue);
                    if (matcher.matches()) {
                        updateVideoProfileOnUiThread(matcher.group("videoprofile").trim());
                    }
                }

            }
        };
        statusProperty().addListener(listener);
        execute(new VideoInfoParameters(downloadData.getUrl()), false);
        if (!findTitle[0]) {
            updateTitleOnUiThread("错误的视频网址");
        }
        statusProperty().removeListener(listener);
    }

    private void updateVideoProfileOnUiThread(String profile) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                System.out.println("profile:" + profile);
                videoProfile.set(profile);
            }

        });
    }

    private void updateTitleOnUiThread(String title) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                videoTitle.set(title);
            }

        });
    }

    private void updateDownloadData(DownloadData downloadData) {
        if (this.downloadData != null) {
            this.downloadData.progressProperty().unbind();
            this.downloadData.statusProperty().unbind();
            this.downloadData.titleProperty().unbind();
            this.downloadData.videoProfileProperty().unbind();
            this.downloadData.speedProperty().unbind();
        }

        updateTitleOnUiThread(downloadData.getTitle());
        updateVideoProfileOnUiThread(downloadData.getVideoProfile());
        updateProgressOnUiThread(0, 0);
        updateSpeedOnUiThread("");

        this.downloadData = downloadData;
        this.downloadData.progressProperty().bind(progress);
        this.downloadData.statusProperty().bind(progressStatus);
        this.downloadData.titleProperty().bind(videoTitle);
        this.downloadData.videoProfileProperty().bind(videoProfile);
        this.downloadData.speedProperty().bind(speed);
    }

    private void updateSpeedOnUiThread(String speed) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                VideoDownload.this.speed.set(speed);
            }

        });
    }

    public void download(DownloadData downloadData) {
        updateDownloadData(downloadData);

        ChangeListener<String> listener = new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (Debug.LOG) {
                    System.out.println(newValue);
                }

                {
                    Matcher matcher = TITLE_REGEX.matcher(newValue);
                    if (matcher.matches()) {
                        updateTitleOnUiThread(matcher.group("title").trim());
                    }
                }

                {
                    Matcher matcher = VIDEO_PROFILE_REGEX.matcher(newValue);
                    if (matcher.matches()) {
                        updateVideoProfileOnUiThread(matcher.group("videoprofile").trim());
                    }
                }

                for (String split : newValue.split(" ")) {
                    Matcher matcher = PROGRESS_REGEX.matcher(split);
                    if (matcher.matches()) {
                        updateProgressOnUiThread(Double.parseDouble(matcher.group("downloaded")), Double.parseDouble(matcher.group("total")));
                    }
                }

                {
                    Matcher matcher = SPEED_REGEX.matcher(newValue);
                    if (matcher.matches()) {
                        updateSpeedOnUiThread(matcher.group("speed"));
                    }
                }
            }
        };
        statusProperty().addListener(listener);
        isDownloading.set(true);

        boolean isFirstRun = true;
        while (isFirstRun || shouldRestartDownload) {
            isFirstRun = false;
            shouldRestartDownload = false;

            // reset download status
            updateProgressOnUiThread(0, 0);

            execute(new VideoDownloadParameters(downloadData.getDownloadDir(), downloadData.getUrl()), false);
        }

        isDownloading.set(false);
        statusProperty().removeListener(listener);
    }

    private void updateProgressOnUiThread(double downloadedSize, double totalSize) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                VideoDownload.this.downloadedSize.set(downloadedSize);
                VideoDownload.this.totalSize.set(totalSize);
            }

        });
    }

    private DownloadData downloadData;

    private boolean shouldRestartDownload = false;

    private final BooleanProperty isDownloading = new SimpleBooleanProperty();

    private final DoubleProperty downloadedSize = new SimpleDoubleProperty();

    private final DoubleProperty totalSize = new SimpleDoubleProperty();

    private final StringProperty videoProfile = new SimpleStringProperty();

    private final StringProperty videoTitle = new SimpleStringProperty();

    private final ObservableDoubleValue progress = downloadedSize.divide(totalSize);

    private final ObservableStringValue progressStatus = downloadedSize.asString().concat("/").concat(totalSize.asString()).concat(" MB");

    private static final Pattern VIDEO_PROFILE_REGEX = Pattern.compile(".+video-profile:(?<videoprofile>.+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern PROGRESS_REGEX = Pattern.compile("\\(?(?<downloaded>[\\d\\.]+)/(?<total>[\\d\\.]+)MB\\)", Pattern.CASE_INSENSITIVE);

    private static final Pattern TITLE_REGEX = Pattern.compile(".*((title)|(playlist)):(?<title>.+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern SPEED_REGEX = Pattern.compile(".+ (?<speed>\\d+ ((kB)|(MB))/s)$", Pattern.CASE_INSENSITIVE);

    private final StringProperty speed = new SimpleStringProperty();

    private class ProgressChecker extends Thread {

        public ProgressChecker() {
            setDaemon(true);
        }

        private final long CHECK_INTERVAL = 20;

        // MB/s
        private final double MIN_DOWNLOAD_SPEED = 0.05;

        private final double MIN_DOWNLOAD_SIZE = CHECK_INTERVAL * MIN_DOWNLOAD_SPEED;

        private double lastDownloadedSize;

        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(CHECK_INTERVAL * 1000);

                    if (!isDownloading.get()) {
                        continue;
                    }

                    if (Debug.LOG) {
                        System.out.println("check: " + lastDownloadedSize + " " + downloadedSize.get() + "/" + totalSize.get());
                    }

                    // if download not start yet
                    if (totalSize.get() < 1) {
                        continue;
                    }

                    // if video is merging
                    if (totalSize.get() - downloadedSize.get() < 1) {
                        continue;
                    }

                    // if something is wrong, EX: download restart
                    if (lastDownloadedSize > downloadedSize.get()) {
                        lastDownloadedSize = downloadedSize.get();
                        continue;
                    }

                    if (downloadedSize.get() - lastDownloadedSize < MIN_DOWNLOAD_SIZE) {
                        System.out.println("restart download");
                        shouldRestartDownload = true;
                        forceCancel();
                    }

                    lastDownloadedSize = downloadedSize.get();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
