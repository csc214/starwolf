package starwolf;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

/**
 * Created by Timothy Haskins on 5/17/17.
 */
public class HistogramCanvas extends Canvas {
    private short[] histogram_data;

    public HistogramCanvas() {
        super(256, 100);
        initHistogramData();
    }

    void initHistogramData() {
        histogram_data = new short[256];
        for (int i = 0; i < 256; ++i) {
            histogram_data[i] = 0;
        }
    }

    void render(short[] buffer) {
        initHistogramData();
        getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
        getGraphicsContext2D().setStroke(Color.BLACK);

        for (int i = 0; i < buffer.length; ++i) ++histogram_data[buffer[i]];

        for(int i = 0; i < 256; ++i){
            getGraphicsContext2D().beginPath();
            getGraphicsContext2D().moveTo(i, 0);
            getGraphicsContext2D().lineTo(i, histogram_data[i]);
            getGraphicsContext2D().closePath();
            getGraphicsContext2D().stroke();
        }
    }
}
