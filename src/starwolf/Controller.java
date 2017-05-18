package starwolf;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import nom.tam.fits.*;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.BufferedFile;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

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
    private Slider log_slider;
    @FXML
    private Label log_val;
    @FXML
    private Slider gamma_slider;
    @FXML
    private Label gamma_val;
    @FXML
    private HistogramCanvas histogram;
    @FXML
    private TextField tfXSIZE;
    @FXML
    private TextField tfXMSEC;
    @FXML
    private TextField tfXFRAME;
    @FXML
    private TextField tfYFRAME;
    @FXML
    private TextField tfYSIZE;

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
        log_slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                canvas.logTransform(t1.doubleValue());
                log_val.setText(t1.toString());
            }
        });
        gamma_slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                canvas.gammaTransform(t1.doubleValue());
                gamma_val.setText(t1.toString());
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
        serialLink.write("XSIZE?");
        try {
            // thread to sleep for 1000 milliseconds
            Thread.sleep(500);
        } catch (Exception e) {
            System.out.println(e);
        }
        tfXSIZE.setText(String.valueOf(canvas.xsize));
        serialLink.write("YSIZE?");
        try {
            // thread to sleep for 1000 milliseconds
            Thread.sleep(500);
        } catch (Exception e) {
            System.out.println(e);
        }
        tfYSIZE.setText(String.valueOf(canvas.ysize));
        serialLink.write("XFRAME");
        try {
            // thread to sleep for 1000 milliseconds
            Thread.sleep(500);
        } catch (Exception e) {
            System.out.println(e);
        }
        tfXFRAME.setText(String.valueOf(canvas.xframe));
        serialLink.write("YFRAME");
        try {
            // thread to sleep for 1000 milliseconds
            Thread.sleep(500);
        } catch (Exception e) {
            System.out.println(e);
        }
        tfYFRAME.setText(String.valueOf(canvas.yframe));
        serialLink.write("XMSEC?");
        try {
            // thread to sleep for 1000 milliseconds
            Thread.sleep(500);
        } catch (Exception e) {
            System.out.println(e);
        }
        tfXMSEC.setText(String.valueOf(canvas.xmsec));
    }
    @FXML
    protected void grImg() {
        serialLink.write("GRIMG");
    }
    @FXML
    protected void upXSIZE() {
        canvas.xsize = Integer.valueOf(tfXSIZE.getText());
        serialLink.write("XSIZE " + tfXSIZE.getText());
    }
    @FXML
    protected void upYSIZE() {
        canvas.ysize = Integer.valueOf(tfYSIZE.getText());
        serialLink.write("YSIZE " + tfXSIZE.getText());
    }
    @FXML
    protected void upXFRAME() {
        canvas.xframe = Integer.valueOf(tfXFRAME.getText());
        serialLink.write("XFRAME " + tfXFRAME.getText());
    }
    @FXML
    protected void upYFRAME() {
        canvas.yframe = Integer.valueOf(tfYFRAME.getText());
        serialLink.write("YFRAME " + tfYFRAME.getText());
    }
    @FXML
    protected void upXMSEC() {
        canvas.xmsec = Integer.valueOf(tfXMSEC.getText());
        serialLink.write("XMSEC " + tfXMSEC.getText());
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
            ext = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());

            if (ext.matches("^[Ff][Ii]?[Tt][Ss]?$")) {
                Fits f = new Fits(file);
                BasicHDU basichdu = f.getHDU(0);
                ImageHDU imghdu = (ImageHDU) basichdu;
                //get out the pixel array and the dimensions
                ImageData imageData = imghdu.getData();
                int x = imghdu.getAxes()[0];
                int y = imghdu.getAxes()[1];
                Object data = imageData.getData();
                if (data instanceof short[][]) {
                    short[][] tmpData = (short[][]) data;
                    short[][] tmp = new short[x][y];
                    for (int jhat = 0; jhat < y; ++jhat) {
                        for (int ihat = 0; ihat < x; ++ihat) {
                            tmp[ihat][jhat] = (short) (((int) ((int) (tmpData[ihat][jhat] & 0xffff) - imghdu.getBZero()) & 0xffff) / imghdu.getBZero() * 255);
                        }
                    }
                    canvas.fillBuffer(tmp);
                }
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
        statusLeft.setText("Saving File ... ");

        try {
            FileChooser fileChooser = new FileChooser();
            File file;
            String fileName, ext;

            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fits", "*.fits"));

            //Show save file dialog
            file = fileChooser.showSaveDialog(null);
            fileName = file.getName();
            ext = fileName.substring(fileName.lastIndexOf("."), fileName.length()).trim();

            if (ext.matches("^\\.[Ff][Ii]?[Tt][Ss]?$")) {
                BasicHDU basicHDU = FitsFactory.hduFactory(canvas.getBufferTwoD());
                basicHDU.getHeader().setBitpix(8);
                basicHDU.getHeader().setNaxis(2, (int) canvas.getHeight());

                short[][] tmpData = canvas.getBufferTwoD();
                short[][] tmpDataTransposed = new short[(int) canvas.getHeight()][(int) canvas.getWidth()];

                for (int j = 0; j < canvas.getHeight(); ++j)
                    for (int i = 0; i < canvas.getWidth(); ++i)
                        tmpDataTransposed[j][i] = tmpData[i][j];

                BufferedFile bf = new BufferedFile(file.getAbsolutePath(), "rw");
                basicHDU.getHeader().write(bf);

                for (int j = 0; j < canvas.getHeight(); ++j)
                    bf.write(tmpDataTransposed[j]);

                FitsUtil.pad(bf, (long) (canvas.getHeight() * canvas.getWidth()));
                bf.close();
            } else {
                WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", file);
            }
            statusLeft.setText("OK");
        } catch (FitsException | IOException e) {
            e.printStackTrace();
        }
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
    }

    @FXML
    private void renderHistogram(ActionEvent event) {
        histogram.render(canvas.getBufferOneD());
    }
}