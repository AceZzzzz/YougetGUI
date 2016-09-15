package com.getting.yougetgui;

import com.getting.util.Looper;
import com.getting.util.PathRecord;
import com.getting.util.Task;
import com.getting.util.binding.NullableObjectStringFormatter;
import download.LiveStreamDownloadParameter;
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private static final Object MSG_DOWNLOAD = new Object();
    private final PathRecord pathRecord = new PathRecord(getClass(), "download directory");
    private final VideoDownload videoDownload = new VideoDownload();
    private final Looper downloadLooper = new Looper();
    @FXML
    public NotificationPane notification;
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
    @FXML
    private TableColumn<VideoDownloadParameter, String> downloadSpeedColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        videoProfileColumn.setCellValueFactory(new PropertyValueFactory<>("videoProfile"));
        downloadStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        downloadProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        downloadProgressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());
        downloadDirectoryColumn.setCellValueFactory(new PropertyValueFactory<>("downloadDirectory"));
        downloadSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("speed"));
        videoTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        downloadDirectoryView.textProperty().bind(new NullableObjectStringFormatter<>(pathRecord.pathProperty()));
    }

    @FXML
    public void onAddLiveStreamUrlClick() {
        addExitListener();

        VideoUrlInputDialog videoUrlInputDialog = new VideoUrlInputDialog();
        videoUrlInputDialog.setTitle("新建直播下载");
        videoUrlInputDialog.initOwner(downloadList.getScene().getWindow());
        videoUrlInputDialog.showAndWait().ifPresent(s -> addLiveStreamDownloadTask(s.split("\n")));
    }

    private void addDownloadTask(String[] urls) {
        for (String url : urls) {
            url = url.trim();
            if (url.isEmpty()) {
                continue;
            }

            VideoDownloadParameter videoDownloadParameter = new VideoDownloadParameter(url, pathRecord.getPath());
            downloadList.getItems().add(videoDownloadParameter);
            downloadLooper.postTask(new DownloadTask(videoDownloadParameter, false));
        }
    }

    private void addLiveStreamDownloadTask(String[] urls) {
        for (String url : urls) {
            url = url.trim();
            if (url.isEmpty()) {
                continue;
            }

            LiveStreamDownloadParameter videoDownloadParameter = new LiveStreamDownloadParameter(url, pathRecord.getPath());
            downloadList.getItems().add(videoDownloadParameter);
            downloadLooper.postTask(new DownloadTask(videoDownloadParameter, true));
        }
    }

    private void addExitListener() {
        downloadList.getScene().getWindow().setOnCloseRequest(event -> {
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
        addExitListener();

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
        File directory = directoryChooser.showDialog(downloadList.getScene().getWindow());
        if (directory == null) {
            return;
        }

        pathRecord.set(directory);
    }

    private class DownloadTask extends Task {

        private final VideoDownloadParameter videoDownloadParameter;
        private final boolean infinite;

        DownloadTask(VideoDownloadParameter videoDownloadParameter, boolean infinite) {
            super(MSG_DOWNLOAD, 0);
            this.videoDownloadParameter = videoDownloadParameter;
            this.infinite = infinite;
        }

        @Override
        public void run() {
            videoDownload.download(videoDownloadParameter, infinite);
        }

        @Override
        public void cancel() {
            videoDownload.cancel();
        }

    }

}
