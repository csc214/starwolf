package starwolf;

import java.awt.*;
import java.io.*;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Scanner;

/**
 * Created by fig on 3/30/2017.
 */
public class Connector implements SerialPortEventListener{
    public static boolean gettingImage = false;
    SerialPort serialPort;
    TextArea termDisplay;
    InputStream instream;
    SWCanvas currentCanvas;
    /** The port we're normally going to use. */

    private static final String PORT_NAMES[] = {
            "/dev/tty.usbmodem2539041", // Mac OS X
            "/dev/ttyACM0", // Raspberry Pi
            "/dev/ttyUSB0", // Linux
            "COM4", "COM5" // Windows
    };
    /**
     * A BufferedReader which will be fed by a InputStreamReader
     * converting the bytes into characters
     * making the displayed results codepage independent
     */
    private DataInputStream dataIs;
    private Scanner scanner;    /** The output stream to the port */
    private OutputStream output;
    /** Milliseconds to block while waiting for port open */
    private static final int TIME_OUT = 2000;
    /** Default bits per second for COM port. */
    private static final int DATA_RATE = 9600;

    public void initialize(TextArea term) {
        termDisplay = term;
        System.out.println("Begin Connector Init");
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        //First, Find an instance of serial port as set in PORT_NAMES.
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            for (String portName : PORT_NAMES) {
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
                }
            }
        }
        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }

        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                    TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // open the streams
            output = serialPort.getOutputStream();
            instream = serialPort.getInputStream();
            dataIs = new DataInputStream(instream);
            scanner = new Scanner(System.in);
            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);

        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * This should be called when you stop using the port.
     * This will prevent port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    public void write(String mess) {
        System.out.println("writing");
        if(mess.contentEquals("grimg") || mess.contentEquals("grimg_demo")||mess.contentEquals("GRIMG") || mess.contentEquals("GRIMG_DEMO")){
            gettingImage = true;
            System.out.println("gettingImage is true");
        }
        try {
            System.out.println("beginning output");
            output.write((mess + "\n").getBytes());
            output.flush();
            System.out.println("Flushed");
        } catch (Exception e) {
            System.err.println(e.toString());
        }

    }
    /**
     * Handle an event on the serial port. Read the data and print it.
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            if(gettingImage == false) {
                try {
                    byte[] stringBuff = new byte[1024];
                    dataIs.read(stringBuff);
                    termDisplay.appendText(new String(stringBuff, "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                int avail = 0;
                int offset = 0;
                int offtotal = 0;
                try {
                    System.out.println("Retrieving Image");
                    int fullsize = SWCanvas.xsize*SWCanvas.ysize*2;
                    byte[] finalImage = new byte[SWCanvas.xsize*SWCanvas.ysize*2];

                    while(offtotal < SWCanvas.xsize*SWCanvas.ysize*2) {
                        avail = dataIs.available();
                        if (avail + offtotal > fullsize){
                            avail = fullsize - offtotal;
                        }
                        offset = dataIs.read(finalImage, offtotal, avail);
                        if(offset == -1){
                            break;
                        }
                        offtotal+=offset;
                    }
                    System.out.println("Finished byte loop");
                    System.out.println(offtotal);
                    ByteBuffer bb = ByteBuffer.wrap(finalImage).order(ByteOrder.BIG_ENDIAN);
                    ShortBuffer sb = bb.asShortBuffer();
                    short[][] drawAble = new short[SWCanvas.xsize][SWCanvas.ysize];
                    for(int y = 0; y < SWCanvas.ysize; y++){
                    for(int x = 0; x < SWCanvas.xsize; x++) {
                        drawAble[x][y]=sb.get();
                    }
                    }

                    gettingImage = false;
                    currentCanvas.fillBuffer(drawAble);
                } catch (Exception e) {
                    System.err.println(e.toString());
                    System.out.println("avail  " + avail);
                    System.out.println("offset  " + offset);
                    System.out.println("offtotal  " + offtotal);
                }
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }

    public void setCurrentCanvas(SWCanvas passedIn){
        currentCanvas = passedIn;
    }

}
