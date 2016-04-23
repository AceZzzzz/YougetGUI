package sample;

import download.VideoDownload;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ui.SmartDirectoryChooser;
import util.Looper;
import util.Task;
import view.VideoUrlInputDialog;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class Controller implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        downloadProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        downloadDirectoryColumn.setCellValueFactory(new PropertyValueFactory<>("downloadDirectory"));
        videoNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        downloadDirectoryView.textProperty().bind(directoryChooser.lastDirectoryProperty().asString());
    }

    private class DownloadTask extends Task {

        private final DownloadData downloadData;

        public DownloadTask(DownloadData downloadData) {
            super(MSG_DOWNLOAD, 0);
            this.downloadData = downloadData;
        }

        @Override
        public void run() {
            videoDownload.download(downloadData);
        }

        @Override
        public void cancel() {
            videoDownload.forceCancel();
        }

    }

    @FXML
    private void onAddUrl(ActionEvent event) {
        VideoUrlInputDialog videoUrlInputDialog = new VideoUrlInputDialog();
        videoUrlInputDialog.initOwner(downloadList.getScene().getWindow());
        videoUrlInputDialog.showAndWait().ifPresent(new Consumer<String>() {

            @Override
            public void accept(String s) {
                for (String split : s.split("\n")) {
                    DownloadData downloadData = new DownloadData(split, directoryChooser.lastDirectoryProperty().get());
                    downloadList.getItems().add(downloadData);
                }

                // something wrong with get video info
//                for (DownloadData downloadData : downloadList.getItems()) {
//                    Looper.postTask(new UpdateVideoInfoTask(downloadData));
//                }

                for (DownloadData downloadData : downloadList.getItems()) {
                    Looper.postTask(new DownloadTask(downloadData));
                }
            }

        });
    }

    private static final Object MSG_UPDATE_VIDEO_INFO = new Object();

    private class UpdateVideoInfoTask extends Task {

        private final DownloadData downloadData;

        public UpdateVideoInfoTask(DownloadData downloadData) {
            super(MSG_UPDATE_VIDEO_INFO, 0);
            this.downloadData = downloadData;
        }

        @Override
        public void run() {
            videoDownload.updateVideoInfo(downloadData);
        }

        @Override
        public void cancel() {
            videoDownload.forceCancel();
        }

    }

    @FXML
    private void onOpenSaveDirectory(ActionEvent event) {
        try {
            java.awt.Desktop.getDesktop().open(downloadList.getSelectionModel().getSelectedItem().getDownloadDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSetDownloadDirectoryClick() {
        directoryChooser.show(downloadList.getScene().getWindow());
    }

    private final SmartDirectoryChooser directoryChooser = new SmartDirectoryChooser();

    @FXML
    private Label downloadDirectoryView;

    @FXML
    private TableColumn<DownloadData, String> videoNameColumn;

    @FXML
    private TableColumn<DownloadData, File> downloadDirectoryColumn;

    @FXML
    private TableView<DownloadData> downloadList;

    @FXML
    private TableColumn<DownloadData, String> downloadProgressColumn;

    private final VideoDownload videoDownload = new VideoDownload();

    private static final Object MSG_DOWNLOAD = new Object();

}
