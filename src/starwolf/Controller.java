package starwolf;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;


public class Controller {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Canvas mainCanvas;


    @FXML
    void initialize() {
        assert mainCanvas != null : "fx:id=\"mainCanvas\" was not injected: check your FXML file 'view.fxml'.";


    }

}