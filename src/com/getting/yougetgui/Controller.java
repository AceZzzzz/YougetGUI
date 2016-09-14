package com.getting.yougetgui;

import com.getting.util.Looper;
import com.getting.util.PathRecord;
import com.getting.util.Task;
import com.getting.util.binding.NullableObjectStringFormatter;
import download.LiveStreamDownloadParameter;
import download.VideoDownload;
import download.VideoDownloadParameter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
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
        VideoUrlInputDialog videoUrlInputDialog = new VideoUrlInputDialog();
        videoUrlInputDialog.setTitle("新建直播下载");
        videoUrlInputDialog.initOwner(downloadList.getScene().getWindow());
        videoUrlInputDialog.showAndWait().ifPresent(s -> {
            for (String split : s.split("\n")) {
                if (split.trim().isEmpty()) {
                    continue;
                }

                addLiveStreamDownloadTask(split.trim());
                // just the first one is available
                break;
            }
        });
    }

    private void addDownloadTask(String url) {
        VideoDownloadParameter videoDownloadParameter = new VideoDownloadParameter(url, pathRecord.getPath());
        downloadList.getItems().add(videoDownloadParameter);
        downloadLooper.postTask(new DownloadTask(videoDownloadParameter, false));
    }

    private void addLiveStreamDownloadTask(String url) {
        LiveStreamDownloadParameter videoDownloadParameter = new LiveStreamDownloadParameter(url, pathRecord.getPath());
        downloadList.getItems().add(videoDownloadParameter);
        downloadLooper.postTask(new DownloadTask(videoDownloadParameter, true));
    }

    @FXML
    private void onAddUrlClick() {
        VideoUrlInputDialog videoUrlInputDialog = new VideoUrlInputDialog();
        videoUrlInputDialog.setTitle("新建下载");
        videoUrlInputDialog.initOwner(downloadList.getScene().getWindow());
        videoUrlInputDialog.showAndWait().ifPresent(s -> {
            for (String split : s.split("\n")) {
                if (split.trim().isEmpty()) {
                    continue;
                }

                addDownloadTask(split.trim());
            }
        });
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
