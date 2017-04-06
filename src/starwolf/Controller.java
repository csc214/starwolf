package starwolf;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;


public class Controller {
    private Connector serialLink;

    @FXML
    private Canvas canvas;
    @FXML
    private VBox root;
    @FXML
    private TextField terminal;
    @FXML
    private TextArea termOut;

    @FXML
    protected void initialize() {
        System.out.println("initialize");
        serialLink = new Connector();
        serialLink.initialize(termOut);
        Thread t=new Thread() {
            public void run() {
                //the following line will keep this app alive for 1000 seconds,
                //waiting for events to occur and responding to them (printing incoming messages to console).
                try {Thread.sleep(1000000);} catch (InterruptedException ie) {}
            }
        };
        t.start();
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
        String temp = terminal.getText();
        serialLink.write(temp);
        termOut.appendText(temp + "\n");
        terminal.clear();
    }

    @FXML
    public void terminalOut(String temp){
        termOut.appendText(temp + "\n");
        System.out.println(temp);
    }

}