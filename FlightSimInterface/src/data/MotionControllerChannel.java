package data;

import util.Observer;
import util.Utils;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static util.Utils.*;

public class MotionControllerChannel extends Thread {

    private DataChannel dataChannel;
    private Observer observer;
    private DatagramSocket socket;
    private static MotionControllerChannel instance;

    private byte[] data;
    private byte[] buf = new byte[256];
    private double[] displayValues;

    private boolean running;


    private MotionControllerChannel() {
        try {
            socket = new DatagramSocket(8888);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        dataChannel = DataChannel.getInstance();
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buf, buf.length, address, port);

            //notify the data channel we have received data from motion dataChannel
            observer.update();
        }
        dataChannel.relayConnectionLost("MotionController disconnected.");
        socket.close();
    }

    //byte legend

    //DAC CG changes range of voltages the DAC can accept
    //
    //offset, you send a command that you want 0V, but we get 0.1V. Offset would be used to correct that.

    /**
     * 0-1 Pitch Setpoint   13 kP Pitch
     * 2-3 Pitch Value      21 kI Pitch
     * 4-5 Roll Setpoint    29 kD Pitch
     * 6-7 Roll Value       37 kP Roll
     * 8-9 Pitch PWM        45 kI Roll
     * 10-11 Roll PWM       53 kD Roll
     * 12 State             61 inputs
     *                      62 outputs
     * @param packet
     */
    private void extractDataFromUDPPacket(DatagramPacket packet){
        data = packet.getData();
        if (data == null) {
            return;
        }
        this.displayValues = new double[] {
                map((long)getShort(data, 0),0, 12600L,0, 360),
                map((long)getShort(data, 2), 0, 12600L,0, 360),
                map((long)getShort(data, 4),0, 8640L,0, 360),
                map((long)getShort(data, 6), 0, 8640L,0, 360),
                map((long)getShort(data, 8), Integer.MIN_VALUE, Integer.MAX_VALUE, -10, 10),
                map((long)getShort(data, 10), Integer.MIN_VALUE, Integer.MAX_VALUE, -10, 10),
                getByte(data, 12),
                getDouble(data, 13),
                getDouble(data, 21),
                getDouble(data, 29),
                getDouble(data, 37),
                getDouble(data, 45),
                getDouble(data, 53),
                getByte(data, 61),
                getByte(data, 62)
        };
    }

    double[] getDisplayValues(){
        return displayValues;
    }

    void setObserver(Observer observer){
        this.observer = observer;
    }

    void sendCommand(char c){
        sendCommand(new byte[]{(byte)c});
    }

    void sendCommand(double[] cmdVals, char cmd){
        byte[] array = writeToByteArray(cmd, cmdVals);
        sendCommand(array);
    }

    void sendCommand(byte[] cmdVals) {
        try {
            InetAddress address = InetAddress.getByName("192.168.1.6");
            DatagramPacket packet = new DatagramPacket(cmdVals, cmdVals.length, address, 8888);
            socket.send(packet);
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
        dataChannel.relaySuccessToView();
    }

    public static MotionControllerChannel getInstance(){
        if (instance == null) {
            instance = new MotionControllerChannel();
            return instance;
        }
        return instance;
    }

}
