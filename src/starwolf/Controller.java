package starwolf;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import nom.tam.fits.*;

import java.io.File;
import java.io.IOException;

public class Controller {
    private Connector serialLink;
    @FXML
    private VBox root;
    @FXML
    private MenuBar menuBar;
    @FXML
    private HBox mainSpace;
    @FXML
    private VBox toolSpace;
    @FXML
    private TextArea termOut;
    @FXML
    private TextField terminal;
    @FXML
    private SWCanvas canvas;
    private GraphicsContext ctx;
    @FXML
    private HBox footerSpace;
    @FXML
    private Label statusLeft;
    @FXML
    private Label statusRight;
    private Header fitsHeader;
    private double w, h;
    private boolean debug = true;

    @FXML
    protected void initialize() {
        initSerialLink();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        w = screenBounds.getWidth();
        h = screenBounds.getHeight() - 24.0;

        double toolSpaceWidth = 200.0, mainSpaceHeight = h - 48.0;

        ctx = canvas.getGraphicsContext2D();
        root.setPrefSize(w, h);
        mainSpace.setPrefHeight(mainSpaceHeight);
        toolSpace.setPrefWidth(toolSpaceWidth);
        canvas.setHeight(mainSpaceHeight - 18);
        canvas.setWidth(w - toolSpaceWidth - 2);
        statusLeft.setPrefWidth(w / 2.0);
        statusRight.setPrefWidth(w / 2.0);
        statusLeft.setAlignment(Pos.BASELINE_LEFT);
        statusRight.setAlignment(Pos.BASELINE_RIGHT);
        toolSpace.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(0.0, 1.0, 0.0, 0.0))));
        footerSpace.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(1.0, 0.0, 1.0, 0.0))));

        if (debug) System.out.println("initialized");
    }

    @FXML
    protected void menuActionFileOpen(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            File file;
            String fileName, ext;

            fileChooser.setTitle("Open File");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images",
                    "*.jpg", "*.JPG",
                    "*.png", "*.PNG",
                    "*.gif", "*.GIF",
                    "*.fits", "*.FITS",
                    "*.fit", "*.FIT",
                    "*.fts", "*.FTS"
            ));
            file = fileChooser.showOpenDialog(null);
            fileName = file.getName();
            ext = fileName.substring(fileName.lastIndexOf("."), fileName.length());

            if (ext.matches("^\\.[Ff][Ii]?[Tt][Ss]?$")) {
                Fits f = new Fits(file);
                ImageHDU hdu = (ImageHDU) f.getHDU(0);
                ImageData imageData = hdu.getData();

                if (debug) hdu.info(System.out);

                int[] axes = hdu.getAxes();
                canvas.draw(imageData.getData(), axes[0], axes[1]);
            } else {
                Image image = new Image("file://" + file.getPath());
                canvas.draw(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());
            }
        } catch (FitsException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void menuActionFileSave(ActionEvent event) {

    }

    @FXML
    protected void menuActionFileQuit(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    protected void menuActionImageFFT(ActionEvent event) {

    }

    @FXML
    private void terminalAction(ActionEvent event) {
        String temp = terminal.getText();
        serialLink.write(temp);
        termOut.appendText(temp + "\n");
        terminal.clear();
    }

    @FXML
    public void terminalOut(String temp) {
        termOut.appendText(temp + "\n");
        System.out.println(temp);
    }

    private void initSerialLink() {
        serialLink = new Connector();
        serialLink.initialize(termOut);
        Thread t = new Thread() {
            public void run() {
                //the following line will keep this app alive for 1000 seconds,
                //waiting for events to occur and responding to them (printing incoming messages to console).
                try {
                    Thread.sleep(1000000);
                } catch (InterruptedException ie) {
                }
            }
        };
        t.start();
    }
}