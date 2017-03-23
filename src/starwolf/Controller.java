package starwolf;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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
    private Canvas canvas;
    @FXML
    private Font font;
    @FXML
    private Color color;
    @FXML
    private Scene root;
    @FXML
    private BorderPane base;
    @FXML
    private VBox optionsVBox;

    private Stage stage;

    protected void setStage(Stage st) {
        this.stage = st;
        stage.setMaximized(true);

        DoubleBinding heightBinding = base.heightProperty().subtract(base.bottomProperty().getValue().getBoundsInLocal().getHeight());
        DoubleBinding widthBinging = base.widthProperty().subtract(optionsVBox.widthProperty());
        canvas.widthProperty().bind(widthBinging);
        canvas.heightProperty().bind(heightBinding);
    }

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
        File file = fileChooser.showOpenDialog(stage);
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
}