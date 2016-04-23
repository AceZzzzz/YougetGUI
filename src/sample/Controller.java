package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ui.SmartDirectoryChooser;
import util.Looper;
import util.Task;
import download.VideoDownload;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class Controller implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        videoUrlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        downloadProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        downloadDirectoryColumn.setCellValueFactory(new PropertyValueFactory<>("saveDir"));
        videoNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        startDownloadView.disableProperty().bind(videoDownload.isDownloadingProperty());
        stopDownloadView.disableProperty().bind(videoDownload.isDownloadingProperty().not());
        downloadDirectoryView.textProperty().bind(directoryChooser.lastDirectoryProperty().asString());
    }

    @FXML
    private void onStartDownloadClick(ActionEvent event) {
        Looper.removeTask(MSG_DOWNLOAD);

        for (DownloadData downloadData : downloadList.getItems()) {
            Looper.postTask(new DownloadTask(downloadData));
        }
    }

    private class DownloadTask extends Task {

        private final DownloadData downloadData;

        public DownloadTask(DownloadData downloadData) {
            super(MSG_DOWNLOAD, 0);
            this.downloadData = downloadData;
        }

        @Override
        public void run() {
            videoDownload.save(downloadData);
        }

        @Override
        public void cancel() {
            videoDownload.forceCancel();
        }

    }

    @FXML
    private void onAddUrl(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("新建下载");
//        dialog.getDialogPane().setContentText("What is your name?");
        dialog.getDialogPane().setHeaderText("输入下载链接");
        dialog.initOwner(downloadList.getScene().getWindow());
        dialog.showAndWait().ifPresent(new Consumer<String>() {

            @Override
            public void accept(String s) {
                DownloadData downloadData = new DownloadData(s, directoryChooser.lastDirectoryProperty().get());
                downloadList.getItems().add(downloadData);

                if (videoDownload.isDownloadingProperty().get()) {
                    Looper.postTask(new DownloadTask(downloadData));
                }
            }

        });
    }

    @FXML
    private void onStopDownloadClick(ActionEvent event) {
        videoDownload.forceCancel();
    }

    @FXML
    private void onSetDownloadDirectoryClick() {
        directoryChooser.show(downloadList.getScene().getWindow());
    }

    private SmartDirectoryChooser directoryChooser = new SmartDirectoryChooser();

    @FXML
    private Label downloadDirectoryView;

    @FXML
    private Button startDownloadView;

    @FXML
    private Button stopDownloadView;

    @FXML
    private TableColumn<DownloadData, String> videoNameColumn;

    @FXML
    private TableColumn<DownloadData, File> downloadDirectoryColumn;

    @FXML
    private TableView<DownloadData> downloadList;

    @FXML
    private TableColumn<DownloadData, String> videoUrlColumn;

    @FXML
    private TableColumn<DownloadData, String> downloadProgressColumn;

    private VideoDownload videoDownload = new VideoDownload();

    private static final Object MSG_DOWNLOAD = new Object();

}
