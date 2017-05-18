package starwolf;

import java.io.*;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import javafx.scene.control.TextArea;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;


import java.util.*;
import java.util.List;

/**
 * Created by fig on 3/30/2017.
 */
public class Connector implements SerialPortEventListener{
    public static String command = "none";
    SerialPort serialPort;
    TextArea termDisplay;
    InputStream instream;
    SWCanvas currentCanvas;
    private static HashMap portMap = new HashMap();
    static String response = null;
    /** The port we're normally going to use. */


    public static List listPorts() {
        Enumeration ports = null;
        ports = CommPortIdentifier.getPortIdentifiers();
        List<String> portList = new ArrayList<String>();
        while (ports.hasMoreElements()) {
            CommPortIdentifier curPort = (CommPortIdentifier) ports.nextElement();
            //get only serial ports
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                portList.add(curPort.getName());
                portMap.put(curPort.getName(), curPort);

            }
        }
        return portList;
    }
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
    }

    public void connectToCamera(String selectedPort){

        try {
            // open serial port, and use class name for the appName.
            CommPortIdentifier portId = (CommPortIdentifier)portMap.get(selectedPort);
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
        if(mess.matches(".*(grimg|grimg_demo|GRIMG|GRIMG_DEMO).*")){
            command = "GRIMG";
            System.out.println("getting Image");
        }
        if(mess.matches(".*(xsize?|ysize?|xframe?|yframe?).*")){
            command = mess;
            System.out.println("getting:" + mess);
        }
        try {
            output.write((mess + "\n").getBytes());
            output.flush();
        } catch (Exception e) {
            System.err.println(e.toString());
        }

    }
    /**
     * Handle an event on the serial port. Read the data and print it.
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            if(command.contentEquals("none")) {
                try {
                    byte[] stringBuff = new byte[1024];
                    dataIs.read(stringBuff);
                    String rawString = new String(stringBuff, "UTF-8");
                    //termDisplay.appendText(rawString);
                    rawString = rawString.trim();
                    if(rawString.endsWith(" OK")) {
                        //response = rawString.substring(0, rawString.length() - 3);
                    } else {
                        //response = rawString;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(command.contentEquals("GRIMG")) {
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
                        System.out.println(offtotal);
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

                    command = "none";
                    currentCanvas.fillBuffer(drawAble);
                } catch (Exception e) {
                    System.err.println(e.toString());
                    System.out.println("avail  " + avail);
                    System.out.println("offset  " + offset);
                    System.out.println("offtotal  " + offtotal);
                }
            } else {
                try {
                    byte[] stringBuff = new byte[32];
                    dataIs.read(stringBuff);
                    //String rawString = new String(stringBuff, "UTF-8");
                    //rawString = rawString.trim();
                    /*if(rawString.endsWith(" OK")) {
                        response = rawString.substring(0, rawString.length() - 3);
                    } else {
                        response = rawString;
                    }*/

                } catch (Exception e) {
                    e.printStackTrace();
                }
                switch(command){
                    case "xsize?":
                        SWCanvas.xsize = Integer.parseInt(response);
                        break;

                    case "ysize?":
                        SWCanvas.ysize = Integer.parseInt(response);
                        break;

                    case "xframe?":
                        SWCanvas.xframe = Integer.parseInt(response);
                        break;

                    case "yframe?":
                        SWCanvas.yframe = Integer.parseInt(response);
                        break;

                }
                //System.out.print(response);
                command = "none";
            }
        }

        if (oEvent.getEventType() == SerialPortEvent.OUTPUT_BUFFER_EMPTY){
            System.out.println("OUTPUT BUFFER EMPTY");
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }

    public void setCurrentCanvas(SWCanvas passedIn){
        currentCanvas = passedIn;
    }

}
