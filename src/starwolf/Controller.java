package starwolf;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;

import java.io.File;
import java.io.IOException;

public class Controller {
    private Connector serialLink;
    @FXML
    private VBox root;
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
    @FXML
    private Label statusLeft;
    @FXML
    private ComboBox portBox;
    @FXML
    private Slider slider;
    @FXML
    private HistogramCanvas histogram;

    @FXML
    protected void initialize() {
        double w, h;
        initSerialLink();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        w = screenBounds.getWidth();
        h = screenBounds.getHeight() - 24.0;

        double toolSpaceWidth = 350.0, mainSpaceHeight = h - 48.0;

        root.setPrefSize(w, h);
        mainSpace.setPrefHeight(mainSpaceHeight);
        mainSpace.setPickOnBounds(false);
        toolSpace.setPrefWidth(toolSpaceWidth);
        toolSpace.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(0.0, 1.0, 0.0, 0.0))));

        statusLeft.setText("Initialized - OK");
        updatePortList();
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                canvas.logTransform(t1.doubleValue());
            }
        });
    }

    @FXML
    protected void updatePortList() {
        portBox.getItems().clear();
        portBox.getItems().addAll(Connector.listPorts());
    }

    @FXML
    protected void cameraConnect() {
        serialLink.connectToCamera((String) portBox.getSelectionModel().getSelectedItem());
    }

    @FXML
    protected void reDraw() {
        canvas.draw();
    }

    @FXML
    protected void grabMeta() {
        Connector.command = "xsize?";
        serialLink.write("xsize?");
    }

    @FXML
    protected void menuActionFileOpen(ActionEvent event) {
        statusLeft.setText("Opening File ... ");

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

                hdu.info(System.out);

                int[] axes = hdu.getAxes();
                canvas.fillBuffer(imageData.getData());
            } else {
                Image image = new Image("file:" + file.getAbsolutePath());
                canvas.setXYSize((int) image.getWidth(), (int) image.getHeight());
                canvas.fillBuffer(image.getPixelReader());
            }

            statusLeft.setText("OK");
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
    protected void menuActionImageCheckerboard(ActionEvent event) {
        int isquares = 8,
                jsquares = 8,
                squarewidth = 20,
                squareheight = 20;
        short black = 0,
                white = 255;
        boolean dark = false;
        short[][] tmp = new short[isquares * squarewidth][jsquares * squareheight];
        for (short j = 0; j < jsquares; ++j) {
            dark = !dark;
            for (short i = 0; i < isquares; ++i) {
                dark = !dark;
                for (short y = 0; y < squareheight; ++y) {
                    for (short x = 0; x < squarewidth; ++x) {
                        if (dark) tmp[squarewidth * i + x][squareheight * j + y] = white;
                        else tmp[squarewidth * i + x][squareheight * j + y] = black;
                    }
                }
            }
        }
        canvas.setXYSize(isquares * squarewidth, jsquares * squareheight);
        canvas.fillBuffer(tmp);
    }

    @FXML
    protected void menuActionImageDeInterlace(ActionEvent event) {
        canvas.deinterlace();
    }

    @FXML
    protected void menuActionImageFHT(ActionEvent event) {
        canvas.fasthartley();
    }

    @FXML
    protected void menuActionImageInvert(ActionEvent event) {
        canvas.invert();
    }

    @FXML
    protected void menuActionImageLogTransform(ActionEvent event) {
        canvas.logTransform(0.25);
    }

    @FXML
    protected void sliderAction(ActionEvent event) {
        System.out.println(slider.getValue());
        canvas.logTransform(slider.getValue());
    }

    @FXML
    private void terminalAction(ActionEvent event) {
        String temp = terminal.getText();
        serialLink.write(temp);
        termOut.appendText(temp + "\n");
        terminal.clear();
    }

    @FXML
    private void initSerialLink() {
        serialLink = new Connector();
        serialLink.initialize(termOut);
        serialLink.setCurrentCanvas(canvas);
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

    @FXML
    private void renderHistogram(ActionEvent event){
        histogram.render(canvas.getBufferOneD());
    }
}