package sample;

import sun.applet.Main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class ControllerChannel extends Thread {

    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];
    private ArrayList<Byte> sendableData;
    private MainController controller;
    private CustomObserver observer;
    private double[] values;
    private DatagramPacket packet;

//    private int pSet, pVal, rSet, rVal, pitchPwm, rollPwm, state;
//    private double kpPitch, kiPitch, kdPitch, kpRoll, kiRoll, kdRoll;

    public ControllerChannel(MainController controller) {
        this.controller = controller;
        try {
            socket = new DatagramSocket(8888);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void sendUpdatedValues(double[] values){

    }


    public void run() {
        running = true;
        while (running) {
            this.packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buf, buf.length, address, port);
            byte[] data = packet.getData();

            initData(data);


//                String received
//                        = new String(packet.getData(), 0, packet.getLength());


//                if (received.equals("end")) {
//                    running = false;
//                    continue;
//                }
//
//                try {
//                    socket.send(packet);
//                } catch (Exception e){
//                    e.printStackTrace();
//                }
        }
        socket.close();
    }

    //byte legend
//0-1 Pitch Setpoint
//2-3 Pitch Value
//4-5 Roll Setpoint
//6-7 Roll Value
//8 Pitch PWM
//9 Roll PWM
//10 State
//11 kP Pitch
//19 kI Pitch
//27 kD Pitch
//35 kP Roll
//43 kI Roll
//51 kD Roll
    private void initData(byte[] data) {
//        this.pSet = getShort(data, 0);
//        this.pVal = getShort(data, 2);
//        this.rSet = getShort(data, 4);
//        this.rVal = getShort(data, 6);
//        this.pitchPwm = getByte(data, 8);
//        this.rollPwm = getByte(data, 9);
//        this.state = getByte(data, 10);
//        this.kpPitch = getDouble(data, 11);
//        this.kiPitch = getDouble(data, 19);
//        this.kdPitch = getDouble(data, 27);
//        this.kpRoll = getDouble(data, 35);
//        this.kiRoll = getDouble(data, 43);
//        this.kpRoll = getDouble(data, 51);
        this.values = new double[] {
                getShort(data, 0),
                getShort(data, 2),
                getShort(data, 4),
                getShort(data, 6),
                getByte(data, 8),
                getByte(data, 9),
                getByte(data, 10),
                getDouble(data, 11),
                getDouble(data, 19),
                getDouble(data, 27),
                getDouble(data, 35),
                getDouble(data, 43),
                getDouble(data, 51)};
        //model.setValues(values);
    }

    public void setValues(double[] data){
        this.values = data;
        notifyObserver();
    }

    public double[] getValues(){
        return values;
    }

    public void addObserver(CustomObserver observer){
        this.observer = observer;
    }

    public void notifyObserver(){
        observer.update();
    }

    public void sendCommand(char cmd){

    }

    public void onValuesUpdated(double[] values){
        //channel.sendUpdatedValues(values);
    }

    private void convertToByteArray(double[] values){

    }

    private byte[] convertDoubleToByteArray(double value){
        byte[] output = new byte[8];
        long lng = Double.doubleToLongBits(value);
        for(int i = 0; i < 8; i++){
            output[i] = (byte)((lng >> ((7 - i) * 8)) & 0xff);
        }
        return output;
    }

    private double getByte(byte[] data, int offset) {
        return (double) ByteBuffer.wrap(data, offset, 1).order(ByteOrder.LITTLE_ENDIAN).getShort();
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
