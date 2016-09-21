package com.getting.yougetgui;

import com.getting.util.AsyncTask;
import com.getting.util.Looper;
import com.getting.util.PathRecord;
import com.getting.util.Task;
import com.getting.util.binding.NullableObjectStringFormatter;
import com.sun.istack.internal.NotNull;
import download.VideoDownload;
import download.VideoDownloadParameter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;
import view.VideoUrlInputDialog;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private static final File DOWNLOAD_HISTORY_FILE = new File(System.getProperty("java.io.tmpdir"), "youget.history");
    private final PathRecord pathRecord = new PathRecord(getClass(), "download directory");
    private final VideoDownload videoDownload = new VideoDownload();
    private final Looper downloadLooper = new Looper();
    private final Looper downloadHistoryLooper = new Looper();
    @FXML
    public NotificationPane notification;
    @FXML
    public Label downloadSpeedView;
    @FXML
    private Label downloadDirectoryView;
    @FXML
    private TableColumn<VideoDownloadParameter, String> videoTitleColumn;
    @FXML
    private TableColumn<VideoDownloadParameter, String> videoProfileColumn;
    @FXML
    private TableColumn<VideoDownloadParameter, File> downloadDirectoryColumn;
    @FXML
    private TableView<VideoDownloadParameter> downloadList;
    @FXML
    private TableColumn<VideoDownloadParameter, String> downloadStatusColumn;
    @FXML
    private TableColumn<VideoDownloadParameter, Double> downloadProgressColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        videoProfileColumn.setCellValueFactory(new PropertyValueFactory<>("videoProfile"));
        downloadStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        downloadProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        downloadProgressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());
        downloadDirectoryColumn.setCellValueFactory(new PropertyValueFactory<>("downloadDirectory"));
        videoTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        downloadDirectoryView.textProperty().bind(new NullableObjectStringFormatter<>(pathRecord.pathProperty()));
        downloadSpeedView.textProperty().bind(videoDownload.speedProperty());

        downloadHistoryLooper.postTask(new ReadDownloadHistoryTask());
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                addExitListener();
            }

        });
    }

    private void addDownloadTask(@NotNull String[] urls) {
        ArrayList<VideoDownloadParameter> params = new ArrayList<>();
        for (String url : urls) {
            url = url.trim();
            if (url.isEmpty()) {
                continue;
            }

            params.add(new VideoDownloadParameter(url, pathRecord.getPath()));
        }

        addDownloadTask(params);
    }

    private void addDownloadTask(@NotNull ArrayList<VideoDownloadParameter> params) {
        for (VideoDownloadParameter param : params) {
            downloadList.getItems().add(param);
            downloadLooper.postTask(new DownloadTask(param));
            downloadLooper.postTask(new WriteHistoryTask());
        }

        downloadList.requestFocus();
        downloadList.getSelectionModel().selectLast();
    }

    private void addExitListener() {
        downloadList.getScene().getWindow().setOnCloseRequest(event -> {
            downloadHistoryLooper.postTask(new WriteHistoryTask());

            if (downloadLooper.isAllDone()) {
                return;
            }

            event.consume();

            notification.getActions().clear();
            notification.getActions().add(new Action("退出", actionEvent -> {
                downloadLooper.removeAllTasks();
                Platform.exit();
            }));
            notification.show("还有视频在下载，确认退出？");
        });
    }

    @FXML
    private void onAddUrlClick() {
        VideoUrlInputDialog videoUrlInputDialog = new VideoUrlInputDialog();
        videoUrlInputDialog.setTitle("新建下载");
        videoUrlInputDialog.initOwner(downloadList.getScene().getWindow());
        videoUrlInputDialog.showAndWait().ifPresent(s -> addDownloadTask(s.split("\n")));
    }

    @FXML
    private void onOpenDownloadDirectoryClick() {
        if (downloadList.getSelectionModel().isEmpty()) {
            return;
        }

        try {
            java.awt.Desktop.getDesktop().open(downloadList.getSelectionModel().getSelectedItem().getDownloadDirectory());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSetDownloadDirectoryClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        if (pathRecord.getPath().isDirectory()) {
            directoryChooser.setInitialDirectory(pathRecord.getPath());
        }
        File directory = directoryChooser.showDialog(downloadList.getScene().getWindow());
        if (directory == null) {
            return;
        }

        pathRecord.set(directory);
    }

    @FXML
    private void onClear() {
        downloadList.getItems().clear();
        downloadLooper.removeAllTasks();
        downloadHistoryLooper.postTask(new WriteHistoryTask());
    }

    private class ReadDownloadHistoryTask extends AsyncTask<ArrayList<VideoDownloadParameter>> {

        public ReadDownloadHistoryTask() {
            super(null, 0);
        }

        @Override
        public ArrayList<VideoDownloadParameter> runTask() {
            if (!DOWNLOAD_HISTORY_FILE.exists()) {
                return new ArrayList<>();
            }

            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(DOWNLOAD_HISTORY_FILE))) {
                return (ArrayList<VideoDownloadParameter>) inputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            return new ArrayList<>();
        }

        @Override
        public void postTaskOnUi(ArrayList<VideoDownloadParameter> result) {
            addDownloadTask(result);
        }

    }

    private class WriteHistoryTask extends Task {

        public WriteHistoryTask() {
            super(null, 0);
        }

        @Override
        public void run() {
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(DOWNLOAD_HISTORY_FILE))) {
                ArrayList<VideoDownloadParameter> parameters = new ArrayList<>();
                parameters.addAll(downloadList.getItems());
                outputStream.writeObject(parameters);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class DownloadTask extends AsyncTask<Void> {

        private final VideoDownloadParameter videoDownloadParameter;

        DownloadTask(VideoDownloadParameter videoDownloadParameter) {
            super(null, 0);
            this.videoDownloadParameter = videoDownloadParameter;
        }

        @Override
        public Void runTask() {
            videoDownload.download(videoDownloadParameter);
            return null;
        }

        @Override
        public void preTaskOnUi() {
            downloadList.scrollTo(videoDownloadParameter);
        }

        @Override
        public void cancel() {
            videoDownload.cancel();
        }

    }

}
