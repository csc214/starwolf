package starwolf;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

/**
 * Created by oeathus on 4/27/17.
 */
public class SWCanvas extends Canvas {
    private int workingBuffer[][];
    private int workingBifferWidth, workingBufferHeight;

    public SWCanvas() {
        super();
        workingBuffer = new int[1024][1024];
    }

    protected int[][] getWorkingBuffer() {
        return workingBuffer;
    }

    protected void setWorkingBuffer(int[][] buffer){
        workingBuffer = buffer;
    }

    protected void draw(Object buffer, int width, int height) {
        workingBuffer = new int[width][height];
        setWidth(width);
        setHeight(height);

        if(buffer instanceof PixelReader){
            PixelReader pixelReader = (PixelReader) buffer;
            for (int y = 0; y < height; ++y)
                for (int x = 0; x < width; ++x)
                    workingBuffer[x][y] = (int)(pixelReader.getColor(x, y).getBrightness()*255);
        } else if(buffer instanceof int[][]){
            setWorkingBuffer((int[][]) buffer);
        }else if(buffer instanceof short[][]){
            short[][] tmpBuffer = (short[][]) buffer;
            for (int y = 0; y < height; ++y)
                for (int x = 0; x < width; ++x)
                    workingBuffer[x][y] = tmpBuffer[x][y];
        }
        draw();
    }

    protected void draw() {
        this.getGraphicsContext2D().clearRect(0, 0, this.getWidth(), this.getHeight());
        for (int y = 0; y < getHeight(); ++y)
            for (int x = 0; x < getWidth(); ++x)
                this.getGraphicsContext2D().getPixelWriter().setColor(x, y, Color.grayRgb(workingBuffer[x][y]));
    }
}
