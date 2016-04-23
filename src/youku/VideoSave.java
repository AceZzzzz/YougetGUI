package youku;

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

public class VideoSave extends Executor {

    public VideoSave() {
        super(VideoSave.class, "you-get-0.4.365-win32.exe");
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

                for (String split : newValue.split(" ")) {
                    Matcher matcher = PROGRESS_REGEX.matcher(split);
                    if (matcher.matches()) {
                        downloaded.set(Double.parseDouble(matcher.group("downloaded")));
                        total.set(Double.parseDouble(matcher.group("total")));
                        updateStatusOnUiThread();
                    }
                }
            }
        };
        statusProperty().addListener(listener);
        isDownload.set(true);

        while (isFirstRun || isForceCancel) {
            isFirstRun = false;
            isForceCancel = false;
            System.out.println("start download");
            execute(new VideoSaveParameters(downloadData.getSaveDir(), downloadData.getUrl()), false);
        }

        isDownload.set(false);
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
                downloadData.setStatus("" + downloaded.get() + "/" + total.get() + " MB");
            }

        });
    }

    private DownloadData downloadData;

    private boolean isFirstRun = true;

    private boolean isForceCancel = false;

    public BooleanProperty isDownloadProperty() {
        return isDownload;
    }

    private BooleanProperty isDownload = new SimpleBooleanProperty();

    private DoubleProperty downloaded = new SimpleDoubleProperty();

    private DoubleProperty total = new SimpleDoubleProperty();

    private static final Pattern PROGRESS_REGEX = Pattern.compile("\\(?(?<downloaded>[\\d\\.]+)/(?<total>[\\d\\.]+)MB\\)", Pattern.CASE_INSENSITIVE);

    private static final Pattern NAME_REGEX = Pattern.compile("title:(?<name>.+)", Pattern.CASE_INSENSITIVE);

    private class ProgressChecker extends Thread {

        public ProgressChecker() {
            setDaemon(true);
        }

        private final long CHECK_INTERVAL = 1 * 60;

        // MB/s
        private final double MIN_DOWNLOAD_SPEED = 0.1;

        private final double MIN_DOWNLOAD_SIZE = CHECK_INTERVAL * MIN_DOWNLOAD_SPEED;

        private double lastDownloaded;

        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(CHECK_INTERVAL * 1000);
                    if (!isDownload.get()) {
                        continue;
                    }

                    System.out.println(lastDownloaded + "," + downloaded);

                    if (lastDownloaded < downloaded.get()) {
                        lastDownloaded = downloaded.get();
                        continue;
                    }

                    if (downloaded.get() - lastDownloaded < MIN_DOWNLOAD_SIZE) {
                        System.out.println("force cancel");
                        isForceCancel = true;
                        forceCancel();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
