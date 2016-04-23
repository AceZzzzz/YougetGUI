package download;

import debug.Debug;
import executor.Executor;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
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
        this.downloadData = downloadData;
        boolean[] findName = new boolean[]{false};
        ChangeListener<String> listener = new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (Debug.LOG) {
                    System.out.println(newValue);
                }

                if (findName[0]) {
                    return;
                }

                {
                    Matcher matcher = TITLE_REGEX.matcher(newValue);
                    if (matcher.matches()) {
                        findName[0] = true;
                        updateNameOnUiThread(matcher.group("name").trim());
                    }
                }
            }
        };
        statusProperty().addListener(listener);
        execute(new VideoInfoParameters(downloadData.getUrl()), false);
        if (!findName[0]) {
            updateNameOnUiThread("错误的视频网址");
        }
        statusProperty().removeListener(listener);
    }

    public void download(DownloadData downloadData) {
        this.downloadData = downloadData;

        ChangeListener<String> listener = new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (Debug.LOG) {
                    System.out.println(newValue);
                }

                {
                    Matcher matcher = TITLE_REGEX.matcher(newValue);
                    if (matcher.matches()) {
                        updateNameOnUiThread(matcher.group("name").trim());
                    }
                }

                for (String split : newValue.split(" ")) {
                    Matcher matcher = PROGRESS_REGEX.matcher(split);
                    if (matcher.matches()) {
                        downloadedSize.set(Double.parseDouble(matcher.group("downloaded")));
                        totalSize.set(Double.parseDouble(matcher.group("total")));
                        updateProgressOnUiThread("" + downloadedSize.get() + "/" + totalSize.get() + " MB");
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
            updateProgressOnUiThread("开始下载");
            execute(new VideoDownloadParameters(downloadData.getDownloadDir(), downloadData.getUrl()), false);
            updateProgressOnUiThread("下载完成");
        }

        isDownloading.set(false);
        statusProperty().removeListener(listener);
    }

    private void updateNameOnUiThread(String name) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                downloadData.setName(name);
            }

        });
    }

    private void updateProgressOnUiThread(String progress) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                downloadData.setProgress(progress);
            }

        });
    }

    private DownloadData downloadData;

    private boolean shouldRestartDownload = false;

    private final BooleanProperty isDownloading = new SimpleBooleanProperty();

    private final DoubleProperty downloadedSize = new SimpleDoubleProperty();

    private final DoubleProperty totalSize = new SimpleDoubleProperty();

    private static final Pattern PROGRESS_REGEX = Pattern.compile("\\(?(?<downloaded>[\\d\\.]+)/(?<total>[\\d\\.]+)MB\\)", Pattern.CASE_INSENSITIVE);

    private static final Pattern TITLE_REGEX = Pattern.compile(".*((title)|(playlist)):(?<name>.+)", Pattern.CASE_INSENSITIVE);

    private class ProgressChecker extends Thread {

        public ProgressChecker() {
            setDaemon(true);
        }

        private final long CHECK_INTERVAL = 30;

        // MB/s
        private final double MIN_DOWNLOAD_SPEED = 0.1;

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
                        updateProgressOnUiThread("重启下载中");
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
