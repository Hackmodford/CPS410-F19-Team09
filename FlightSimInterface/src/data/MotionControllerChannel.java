package data;

import util.CustomObserver;
import util.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MotionControllerChannel extends Thread {

    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];
    private byte[] data;
    private DataChannel controller;
    private CustomObserver observer;
    private double[] displayValues;

    public MotionControllerChannel(DataChannel controller) {
        this.controller = controller;
        try {
            socket = new DatagramSocket(8888);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        running = true;
        while (running) {
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
            data = packet.getData();
            setDisplayValues(data);

            //notify we have received data from observer
            observer.update();
        }
        socket.close();
    }

    //byte legend
//0-1 Pitch Setpoint
//2-3 Pitch Value
//4-5 Roll Setpoint
//6-7 Roll Value
//8-9 Pitch PWM
//10-11 Roll PWM
//12 State
//13 kP Pitch
//21 kI Pitch
//29 kD Pitch
//37 kP Roll
//45 kI Roll
//53 kD Roll
    //DAC CG changes range of voltages the DAC can accept
    //
    //offset, you send a command that you want 0V, but we get 0.1V. Offset would be used to correct that.


    private void setDisplayValues(byte[] data){
        if (data == null) {
            return;
        }
        this.displayValues = new double[] {
                Utils.map((long)getShort(data, 0),0, 12600L,0, 360),
                Utils.map((long)getShort(data, 2), 0, 12600L,0, 360),
                Utils.map((long)getShort(data, 4),0, 8640L,0, 360),
                Utils.map((long)getShort(data, 6), 0, 8640L,0, 360),
                Utils.map((long)getShort(data, 8), Integer.MIN_VALUE, Integer.MAX_VALUE, -10, 10),
                Utils.map((long)getShort(data, 10), Integer.MIN_VALUE, Integer.MAX_VALUE, -10, 10),
                getByte(data, 12),
                getDouble(data, 21),
                getDouble(data, 29),
                getDouble(data, 37),
                getDouble(data, 45),
                getDouble(data, 53)
        };
    }

    double[] getDisplayValues(){
        return displayValues;
    }

    void setObserver(CustomObserver observer){
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
        controller.relaySuccessToView();
    }

    private byte[] convertDoubleToByteArray(double d) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putDouble(d);
        return byteBuffer.array();
    }

    private byte[] writeToByteArray(char cmd, double[] cmdVals){
        byte[] array = new byte[25];
        array[0] = (byte)cmd;
        for (int i = 0; i < cmdVals.length; i++){
            byte[] bytes = convertDoubleToByteArray(cmdVals[i]);
            for (int j = 0; j < bytes.length; j++){
                array[i*8 + j + 1] = bytes[j];
            }
        }
        return array;
    }

    private double getByte(byte[] data, int offset) {
        return (double) (ByteBuffer.wrap(data, offset, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xff);
    }

    private double getShort(byte[] data, int offset) {
        return (double) ByteBuffer.wrap(data, offset, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    private double getInt(byte[] data, int offset) {
        return (double) ByteBuffer.wrap(data, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private double getDouble(byte[] data, int offset) {
        return ByteBuffer.wrap(data, offset, 8).order(ByteOrder.LITTLE_ENDIAN).getDouble();
    }
}
