package starwolf;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;


public class Controller {
    @FXML
    private Canvas canvas;
    @FXML
    private VBox root;
    @FXML
    private TextField terminal;

    @FXML
    protected void initialize() {
        System.out.println("initialized");
    }

    @FXML
    protected void quit(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    protected void fileOpenDialog(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("GIF", "*.gif"),
                new FileChooser.ExtensionFilter("FITS", "*.fits", "*.fit", "*.fts")
        );
        File file = fileChooser.showOpenDialog(null);
        openFile(file);
    }

    private void openFile(final File file) {
        Task t = new Task() {
            @Override
            protected Object call() throws Exception {
                Image tmp = new Image("file:" + file.getAbsolutePath(), canvas.getWidth(), canvas.getHeight(), true, true);
                canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                canvas.getGraphicsContext2D().drawImage(tmp, 0, 0);
                return null;
            }
        };
        new Thread(t).start();
    }

    @FXML
    private void terminalAction(ActionEvent event) {
        System.out.println(terminal.getCharacters());
    }
}