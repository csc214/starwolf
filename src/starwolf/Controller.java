package starwolf;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;


public class Controller implements Initializable{

    @FXML
    private Button openButton;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Canvas mainCanvas;
    private FileChooser fileChooser = new FileChooser();

    @FXML
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert mainCanvas != null : "fx:id=\"mainCanvas\" was not injected: check your FXML file 'view.fxml'.";
        openButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image Files (*.jpg)", "*.jpg");
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showOpenDialog(openButton.getScene().getWindow());
                final Image image = new Image(file.toURI().toString());
                System.out.println(file.toURI().toString());
                mainCanvas.getGraphicsContext2D().drawImage(image,0,0,mainCanvas.getWidth(),(image.getHeight()/image.getWidth())*mainCanvas.getWidth());
            }
        });


    }

}