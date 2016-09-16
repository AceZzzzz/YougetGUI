package view;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

public class VideoUrlInputDialog extends Dialog<String> {

    private final TextArea textArea = new TextArea();

    {
        textArea.setPrefColumnCount(40);
        textArea.setPrefRowCount(10);
        textArea.setWrapText(true);
        textArea.setPromptText("多个下载链接用回车分割");
    }

    public VideoUrlInputDialog() {
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(textArea);
        getDialogPane().setContent(borderPane);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        setResultConverter(param -> param.getButtonData() == ButtonBar.ButtonData.OK_DONE ? textArea.getText() : null);
    }

}
