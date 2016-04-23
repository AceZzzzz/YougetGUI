package view;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

public class VideoUrlInputDialog extends Dialog<String> {

    public VideoUrlInputDialog() {
        setTitle("新建下载");

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(textArea);
        getDialogPane().setContent(borderPane);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        setResultConverter(new Callback<ButtonType, String>() {

            @Override
            public String call(ButtonType param) {
                return param.getButtonData() == ButtonBar.ButtonData.OK_DONE ? textArea.getText() : null;
            }

        });
    }

    private TextArea textArea = new TextArea();

    {
        textArea.setPrefColumnCount(40);
        textArea.setPrefRowCount(10);
        textArea.setPromptText("多个下载链接用回车分割");
    }

}
