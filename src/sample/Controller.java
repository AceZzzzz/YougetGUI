package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import ui.SmartDirectoryChooser;
import util.Looper;
import util.Task;
import youku.VideoSave;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class Controller implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        downloadDirColumn.setCellValueFactory(new PropertyValueFactory<>("saveDir"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        startDownloadView.disableProperty().bind(videoDownload.isDownloadProperty());
        stopDownloadView.disableProperty().bind(videoDownload.isDownloadProperty().not());
    }

    @FXML
    private void onStartDownloadClick(ActionEvent event) {
        Looper.removeTask(MSG_DOWNLOAD);

        for (DownloadData downloadData : downloadList.getItems()) {
            Looper.postTask(new Task(MSG_DOWNLOAD, 0) {

                @Override
                public void cancel() {
                    videoDownload.forceCancel();
                }

                @Override
                public void run() {
                    videoDownload.save(downloadData);
                }

            });
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
                downloadList.getItems().add(new DownloadData(s, SmartDirectoryChooser.getLastDirectory()));
            }

        });
    }

    @FXML
    private void onStopDownloadClick(ActionEvent event) {
        videoDownload.forceCancel();
    }

    @FXML
    private void onSetDownloadDirectoryClick() {
        SmartDirectoryChooser directoryChooser = new SmartDirectoryChooser();
        directoryChooser.show(downloadList.getScene().getWindow());
    }

    @FXML
    private Button startDownloadView;

    @FXML
    private Button stopDownloadView;

    @FXML
    private TableColumn<DownloadData, String> nameColumn;

    @FXML
    private TableColumn<DownloadData, File> downloadDirColumn;

    @FXML
    private TableView<DownloadData> downloadList;

    @FXML
    private TableColumn<DownloadData, String> urlColumn;

    @FXML
    private TableColumn<DownloadData, String> statusColumn;

    private VideoSave videoDownload = new VideoSave();

    private static final Object MSG_DOWNLOAD = new Object();

}
