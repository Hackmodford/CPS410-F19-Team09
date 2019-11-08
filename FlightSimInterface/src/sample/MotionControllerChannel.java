package sample;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class MotionControllerChannel extends Thread {

    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];
    private byte[] data;
    private ArrayList<Byte> sendableData;
    private DataChannel controller;
    private CustomObserver observer;
    private double[] displayValues;
    private DatagramPacket packet;

//    private int pSet, pVal, rSet, rVal, pitchPwm, rollPwm, state;
//    private double kpPitch, kiPitch, kdPitch, kpRoll, kiRoll, kdRoll;

    public MotionControllerChannel(DataChannel controller) {
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
            data = packet.getData();
            setDisplayValues(data);

            //initData(data);


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
            observer.update();
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


    public void setDisplayValues(byte[] data){
        this.displayValues = new double[] {
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
                getDouble(data, 51)
        };
    }

    public double[] getDisplayValues(){
        return displayValues;
    }

    public void setObserver(CustomObserver observer){
        this.observer = observer;
    }

    public void sendCommand(char c){
        sendCommand(new byte[]{(byte)c});
    }

    public void sendCommand(double[] cmdVals, char cmd){
        byte[] array = writeToByteArray(cmd, cmdVals);
        sendCommand(array);
    }

    public void sendCommand(byte[] cmdVals) {
        if (packet == null || socket == null) {
            controller.relayErrorToView("Can't connect to controller.");
            return;
        }
        if (!socket.isConnected()){
            controller.relayErrorToView("Can't connect to controller.");
            return;
        }
        packet.setData(cmdVals);
        try {
            socket.send(packet);
        } catch (Exception e){ e.printStackTrace(); }
        controller.relaySuccessToView();
    }

    private byte[] convertDoubleToByteArray(double d) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeDouble(d);
            dos.flush(); } catch (IOException e) { e.printStackTrace(); }
        return bos.toByteArray();
    }

    private byte[] writeToByteArray(char cmd, double[] cmdVals){
        byte[] array = new byte[25];
        array[0] = (byte)cmd;
        for (int i = 0; i < cmdVals.length; i++){
            byte[] bytes = convertDoubleToByteArray(cmdVals[i]);
            for (int j = 0; j < bytes.length-1; j++){
                array[i*8 + j + 1] = bytes[j];
            }
        }
        return array;
    }

    public void onValuesUpdated(double[] values){
        //channel.sendUpdatedValues(displayValues);
    }

    private void convertToByteArray(double[] values){

    }

//    private byte[] convertDoubleToByteArray(double value){
//        byte[] output = new byte[8];
//        long lng = Double.doubleToLongBits(value);
//        for(int i = 0; i < 8; i++){
//            output[i] = (byte)((lng >> ((7 - i) * 8)) & 0xff);
//        }
//        return output;
//    }

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