package starwolf;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

/**
 * Created by oeathus on 4/27/17.
 */
public class SWCanvas extends Canvas {
    private short originBuffer[][]; // untouched data from the camera
    private short workingBuffer[][];   // one of two swappable buffers actions are performed on
    public static int xsize = 780;  // region of interest width
    public static int ysize = 489;  // region of interest height
    public static int xframe;   // full frame width
    public static int yframe;   // full frame height
    public static int maxGray = 0;  // initialized to zero to capture max value in loop
    public static int minGray = 32767;   // initialized to the max value to capture min value in loop
    public static int desMin;   // used to set black level of image
    public static int desMax;   // used to set white level of image
    public static int xbin, ybin;   // unimplemented:  sets bin size for image binning
    public static double knee;  // exponent of color value to adjust gamma
    public static int temp; // the temperature of the camera at time of capture
    public static int xmsec; // time in milliseconds
    public static String vers; // version of the camera firmware

    public SWCanvas() {
        super();
    }

    public void fillBuffer(Object buffer) {
        workingBuffer = new short[xsize][ysize];

        if (buffer instanceof PixelReader) {
            PixelReader pixelReader = (PixelReader) buffer;
            for (int y = 0; y < ysize; ++y)
                for (int x = 0; x < xsize; ++x)
                    workingBuffer[x][y] = (short) (pixelReader.getColor(x, y).getBrightness() * 255);
            draw();
        } else if (buffer instanceof short[][]) {
            short[][] tmpBuffer = (short[][]) buffer;
            for (int y = 0; y < ysize; ++y)
                for (int x = 0; x < xsize; x++)
                    workingBuffer[x][y] = tmpBuffer[x][y];
            draw();
        }

    }

    protected void draw() {
        this.getGraphicsContext2D().clearRect(0, 0, this.getWidth(), this.getHeight());
        for (int y = 0; y < getHeight(); ++y)
            for (int x = 0; x < getWidth(); ++x)
                this.getGraphicsContext2D().getPixelWriter().setColor(x, y, Color.grayRgb(workingBuffer[x][y]));
    }

    public void setXYSize(int xs, int ys) {
        xsize = xs;
        ysize = ys;
        setWidth(xsize);
        setHeight(ysize);
    }

    protected void deinterlace() {
        short[][] tmp = new short[xsize][ysize];
        for (int i = 0; i < ysize; i += 2)
            for (int j = 0; j < xsize; ++j)
                tmp[j][i] = workingBuffer[j][Math.floorDiv(i, 2)];
        for (int i = 1; i < ysize; i += 2)
            for (int j = 0; j < xsize; ++j)
                tmp[j][i] = workingBuffer[j][Math.floorDiv(ysize, 2) + (int) Math.ceil(i / 2)];
        fillBuffer(tmp);
    }

    protected void fasthartley() {
        int[] data = new int[xsize * ysize];

        for (int i = 0; i < ysize; ++i)
            for (int j = 0; j < xsize; ++j)
                data[j + i * xsize] = workingBuffer[j][i];

        FFT fft = new FFT(data, xsize, ysize);
        java.awt.image.MemoryImageSource tmp = new java.awt.image.MemoryImageSource(xsize, ysize, fft.getPixels(), 0, xsize);
        java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
        java.awt.Image img = toolkit.createImage(tmp);
        java.awt.image.BufferedImage bimage = new java.awt.image.BufferedImage(img.getWidth(null), img.getHeight(null), java.awt.image.BufferedImage.TYPE_INT_ARGB);
        // Draw the image on to the buffered image
        java.awt.Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        Image newImg = SwingFXUtils.toFXImage(bimage, null);
        PixelReader pixelReader = newImg.getPixelReader();

        for (int i = 0; i < ysize; ++i)
            for (int j = 0; j < xsize; ++j)
                workingBuffer[j][i] = (short) (pixelReader.getColor(j, i).getBrightness() * 255);

        draw();
    }
}
