package starwolf;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
    private Canvas canvas;
    private GraphicsContext ctx;
    @FXML
    private HBox footerSpace;
    @FXML
    private Label statusLeft;
    @FXML
    private Label statusRight;
    private double w, h;
    private boolean debug = true;

    @FXML
    protected void initialize() {
        System.out.println("initialize");
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
        System.out.println("initialized");

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        w = screenBounds.getWidth();
        h = screenBounds.getHeight() - 24.0;

        double toolSpaceWidth = 200.0, mainSpaceHeight = h - 48.0;

        ctx = canvas.getGraphicsContext2D();
        root.setPrefSize(w, h);
        toolSpace.setPrefSize(toolSpaceWidth, mainSpaceHeight);
        canvas.setWidth(w - toolSpaceWidth);
        canvas.setHeight(mainSpaceHeight);
        statusLeft.setPrefWidth(w / 2.0);
        statusRight.setPrefWidth(w / 2.0);
        statusLeft.setAlignment(Pos.BASELINE_LEFT);
        statusRight.setAlignment(Pos.BASELINE_RIGHT);
        toolSpace.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(0.0, 1.0, 0.0, 0.0))));
        footerSpace.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(1.0, 0.0, 1.0, 0.0))));

        if (debug) {
            System.out.println("screenBounds.getWidth() -> " + screenBounds.getWidth());
            System.out.println("screenBounds.getHeight() -> " + screenBounds.getHeight());
        }
    }

    @FXML
    protected void menuActionOpen(ActionEvent event) {
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
                canvas.setWidth((double) axes[0]);
                canvas.setHeight((double) axes[1]);

                if (hdu.getBitPix() == 16) {
                    short[][] buffer = (short[][]) imageData.getData();
                    ctx.clearRect(0, 0, (double) axes[0], (double) axes[1]);

                    for (int i = 0; i < axes[0]; ++i) {
                        for (int j = 0; j < axes[1]; ++j) {
                            double color = ((buffer[i][j] + hdu.getBZero())) / ((1 << hdu.getBitPix()) * 1.0);
                            ctx.getPixelWriter().setColor(i, j, Color.color(color, color, color));
                        }
                    }
                }

                if (axes[0] > w || axes[1] > h) {

                }
            } else {
                openFile(file);
            }
        } catch (FitsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @FXML
    protected void quit(ActionEvent event) {
        Platform.exit();
    }

    private void openFile(final File file) {
        Task t = new Task() {
            @Override
            protected Object call() throws Exception {
                Image tmp = new Image("file:" + file.getAbsolutePath(), canvas.getWidth(), canvas.getHeight(), true, true);
                ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                ctx.drawImage(tmp, 0, 0);
                return null;
            }
        };
        new Thread(t).start();
    }
}