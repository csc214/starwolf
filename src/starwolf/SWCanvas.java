package starwolf;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

/**
 * Created by oeathus on 4/27/17.
 */
public class SWCanvas extends Canvas {
    private short workingBuffer[][];
    private int workingBifferWidth, workingBufferHeight;
    public static int xsize = 780;
    public static int ysize = 489;
    public static int max = 0;
    public static int min= 32767;
    public SWCanvas() {
        super();
        workingBuffer = new short[1024][1024];
    }

    protected short[][] getWorkingBuffer() {
        return workingBuffer;
    }

    protected void setWorkingBuffer(short[][] buffer){
        workingBuffer = buffer;
    }


    protected void draw(Object buffer) {
        workingBuffer = new short[xsize][ysize];
        setWidth(xsize);
        setHeight(ysize);

        if(buffer instanceof PixelReader){
            PixelReader pixelReader = (PixelReader) buffer;
            for (int y = 0; y < ysize; ++y)
                for (int x = 0; x < xsize; ++x)
                   // workingBuffer[x][y] = pixelReader.getArgb(x, y);
            draw();
        } else if(buffer instanceof int[][]){
            //setWorkingBuffer((shorbuffer);
            drawColor();
        }else if(buffer instanceof short[][]){
            short[][] tmpBuffer = (short[][]) buffer;
            for (int y = 0; y < ysize; ++y)
                for (int x = 0; x < xsize; x++)
                    workingBuffer[x][y] = tmpBuffer[x][y];
            drawColor();
        }

    }

    protected void draw() {
        this.getGraphicsContext2D().clearRect(0, 0, this.getWidth(), this.getHeight());
        for (int y = 0; y < getHeight(); ++y)
            for (int x = 0; x < getWidth(); ++x)
                this.getGraphicsContext2D().getPixelWriter().setArgb(x, y, workingBuffer[x][y]);
    }
    protected void drawColor() {
        this.getGraphicsContext2D().clearRect(0, 0, this.getWidth(), this.getHeight());
        for (int y = 0; y < getHeight(); ++y)
            for (int x = 0; x < getWidth(); ++x)
                this.getGraphicsContext2D().getPixelWriter().setColor(x, y, Color.grayRgb(workingBuffer[x][y]%255));
    }
}
