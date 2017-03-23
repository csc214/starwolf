package starwolf;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller {
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private Canvas mainCanvas;
    @FXML
    private Font statFont;
    @FXML
    private Color statFill;
    private Stage mainStage;

    protected void setMainStage(Stage stage) {
        mainStage = stage;
    }

    @FXML
    protected void initialize() {
        assert mainCanvas != null : "fx:id=\"mainCanvas\" was not injected: check your FXML file 'view.fxml'.";
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
        File file = fileChooser.showOpenDialog(mainStage);
        openFile(file);
    }

    private void openFile(final File file) {
        Task t = new Task() {
            @Override
            protected Object call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(file.getAbsoluteFile());
                        System.out.println(mainCanvas.getWidth());
                        System.out.println(mainCanvas.getHeight());
                        mainCanvas.getGraphicsContext2D().clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
                        mainCanvas.getGraphicsContext2D().drawImage(new Image("file:" + file.getAbsolutePath()), 0, 0);
                    }
                });
                return null;
            }
        };
        new Thread(t).start();
    }
}