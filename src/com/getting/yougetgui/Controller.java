package com.getting.yougetgui;

import com.getting.util.Looper;
import com.getting.util.PathRecord;
import com.getting.util.Task;
import com.getting.util.binding.NullableObjectStringFormatter;
import download.DownloadData;
import download.VideoDownload;
import javafx.event.ActionEvent;
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
import java.util.function.Consumer;

public class Controller implements Initializable {

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

    private class DownloadTask extends Task {

        private final DownloadData downloadData;

        DownloadTask(DownloadData downloadData) {
            super(MSG_DOWNLOAD, 0);
            this.downloadData = downloadData;
        }

        @Override
        public void run() {
            videoDownload.download(downloadData);
        }

        @Override
        public void cancel() {
            videoDownload.cancel();
        }

    }

    @FXML
    private void onAddUrlClick(ActionEvent event) {
        VideoUrlInputDialog videoUrlInputDialog = new VideoUrlInputDialog();
        videoUrlInputDialog.initOwner(downloadList.getScene().getWindow());
        videoUrlInputDialog.showAndWait().ifPresent(new Consumer<String>() {

            @Override
            public void accept(String s) {
                for (String split : s.split("\n")) {
                    if (split.trim().isEmpty()) {
                        continue;
                    }

                    DownloadData downloadData = new DownloadData(split.trim(), pathRecord.getPath());
                    downloadList.getItems().add(downloadData);
                    Looper.postTask(new DownloadTask(downloadData));
                }
            }

        });
    }

    @FXML
    private void onOpenDownloadDirectoryClick(ActionEvent event) {
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
        pathRecord.set(directory);
    }

    private final PathRecord pathRecord = new PathRecord(getClass());

    @FXML
    private Label downloadDirectoryView;

    @FXML
    private TableColumn<DownloadData, String> videoTitleColumn;

    @FXML
    private TableColumn<DownloadData, String> videoProfileColumn;

    @FXML
    private TableColumn<DownloadData, File> downloadDirectoryColumn;

    @FXML
    private TableView<DownloadData> downloadList;

    @FXML
    private TableColumn<DownloadData, String> downloadStatusColumn;

    @FXML
    private TableColumn<DownloadData, Double> downloadProgressColumn;

    @FXML
    private TableColumn<DownloadData, String> downloadSpeedColumn;

    private final VideoDownload videoDownload = new VideoDownload();

    private static final Object MSG_DOWNLOAD = new Object();

}
