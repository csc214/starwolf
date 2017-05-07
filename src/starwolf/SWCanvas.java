package starwolf;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Created by oeathus on 4/27/17.
 */
public class SWCanvas extends ImageView {
    int workingBuffer[][];
    WritableImage image;
    PixelReader pixelReader;
    PixelWriter pixelWriter;
    int width, height;

    SWCanvas() {
        super();
    }

    int getWidth(){
        return width;
    }
    int getHeight(){
        return height;
    }

    int[][] getWorkingBuffer() {
        return workingBuffer;
    }

    void setWidth(int w){
        width = w;
        setFitWidth(width);
    }

    void setHeight(int h){
        height = h;
        setFitHeight(height);
    }

    void setWorkingBuffer(int[][] buffer) {
        workingBuffer = buffer;
    }

    void draw(Object buffer, int w, int h) {
        setWidth(w);
        setHeight(h);
        workingBuffer = new int[width][height];

        if (buffer instanceof PixelReader) {
            PixelReader pixelReader = (PixelReader) buffer;
            for (int y = 0; y < height; ++y)
                for (int x = 0; x < width; ++x)
                    workingBuffer[x][y] = pixelReader.getArgb(x, y);
        } else if (buffer instanceof int[][]) {
            setWorkingBuffer((int[][]) buffer);
        } else if (buffer instanceof short[][]) {
            short[][] tmpBuffer = (short[][]) buffer;
            for (int y = 0; y < height; ++y)
                for (int x = 0; x < width; ++x)
                    workingBuffer[x][y] = tmpBuffer[x][y];
        }
    }

    void draw(){
        image = new WritableImage(width, height);
        pixelWriter = image.getPixelWriter();
        pixelReader = new PixelReader() {
            @Override
            public PixelFormat getPixelFormat() {
                return PixelFormat.getIntArgbPreInstance();
            }

            @Override
            public int getArgb(int x, int y) {
                return workingBuffer[x][y];
            }

            @Override
            public Color getColor(int x, int y) {
                return Color.grayRgb(workingBuffer[x][y]);
            }

            @Override
            public <T extends Buffer> void getPixels(int x, int y, int w, int h, WritablePixelFormat<T> pixelformat, T buffer, int scanlineStride) {

            }

            @Override
            public void getPixels(int x, int y, int w, int h, WritablePixelFormat<ByteBuffer> pixelformat, byte[] buffer, int offset, int scanlineStride) {

            }

            @Override
            public void getPixels(int x, int y, int w, int h, WritablePixelFormat<IntBuffer> pixelformat, int[] buffer, int offset, int scanlineStride) {

            }
        };

        for (int y = 0; y < height; ++y)
            for (int x = 0; x < width; ++x)
                pixelWriter.setColor(x, y, pixelReader.getColor(x, y));

        setImage(image);
    }
}
