package download;

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

    public void save(DownloadData downloadData) {
        this.downloadData = downloadData;

        ChangeListener<String> listener = new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                {
                    Matcher matcher = NAME_REGEX.matcher(newValue);
                    if (matcher.matches()) {
                        updateNameOnUiThread(matcher.group("name").trim());
                    }
                }

                {
                    Matcher matcher = MERGING_REGEX.matcher(newValue);
                    if (matcher.matches()) {
                        System.out.println("合并文件中");
                    }
                }

                for (String split : newValue.split(" ")) {
                    Matcher matcher = PROGRESS_REGEX.matcher(split);
                    if (matcher.matches()) {
                        downloadedData.set(Double.parseDouble(matcher.group("downloaded")));
                        totalData.set(Double.parseDouble(matcher.group("total")));
                        updateStatusOnUiThread();
                    }
                }
            }
        };
        statusProperty().addListener(listener);
        isDownloading.set(true);

        while (isFirstRun || shouldRestartDownload) {
            isFirstRun = false;
            shouldRestartDownload = false;
            System.out.println("start download");
            execute(new VideoDownloadParameters(downloadData.getSaveDir(), downloadData.getUrl()), false);
            System.out.println("end download");
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

    private void updateStatusOnUiThread() {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                downloadData.setProgress("" + downloadedData.get() + "/" + totalData.get() + " MB");
            }

        });
    }

    private DownloadData downloadData;

    private boolean isFirstRun = true;

    private boolean shouldRestartDownload = false;

    public BooleanProperty isDownloadingProperty() {
        return isDownloading;
    }

    private BooleanProperty isDownloading = new SimpleBooleanProperty();

    private DoubleProperty downloadedData = new SimpleDoubleProperty();

    private DoubleProperty totalData = new SimpleDoubleProperty();

    private static final Pattern PROGRESS_REGEX = Pattern.compile("\\(?(?<downloaded>[\\d\\.]+)/(?<total>[\\d\\.]+)MB\\)", Pattern.CASE_INSENSITIVE);

    private static final Pattern NAME_REGEX = Pattern.compile("title:(?<name>.+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern MERGING_REGEX = Pattern.compile("Merging video parts", Pattern.CASE_INSENSITIVE);

    private class ProgressChecker extends Thread {

        public ProgressChecker() {
            setDaemon(true);
        }

        private final long CHECK_INTERVAL = 10;

        // MB/s
        private final double MIN_DOWNLOAD_SPEED = 0.1;

        private final double MIN_DOWNLOAD_SIZE = CHECK_INTERVAL * MIN_DOWNLOAD_SPEED;

        private double lastDownloadedData;

        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(CHECK_INTERVAL * 1000);

                    if (!isDownloading.get()) {
                        continue;
                    }

                    // if video size not get yet
                    if (totalData.get() < 1) {
                        continue;
                    }

                    System.out.println(lastDownloadedData + ", " + downloadedData.get() + "/" + totalData.get());

                    if (lastDownloadedData > downloadedData.get()) {
                        lastDownloadedData = downloadedData.get();
                        continue;
                    }

                    if (downloadedData.get() - lastDownloadedData < MIN_DOWNLOAD_SIZE) {
                        System.out.println("force cancel");
                        shouldRestartDownload = true;
                        forceCancel();
                    }

                    lastDownloadedData = downloadedData.get();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
