package sample;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MCListener extends Thread {

        private DatagramSocket socket;
        private boolean running;
        private byte[] buf = new byte[256];

        public MCListener() {
            try {
                socket = new DatagramSocket(8888);
            } catch (SocketException e){
                e.printStackTrace();
            }
        }


        public void run() {
            running = true;

            while (running) {
                DatagramPacket packet
                        = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);
                }catch (IOException e){
                    e.printStackTrace();
                    break;
                }

                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                byte[] data = packet.getData();
                int pSetPt = getShort(data, 0);
                int pValue = getShort(data, 2);
                double kpPitch = getDouble(data, 11);
                System.out.println(pSetPt + " " + pValue + " " + kpPitch);


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

        private int getShort(byte[] data, int offset){
            return (int)ByteBuffer.wrap(data, offset, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
        }

        private long getInt(byte[] data, int offset){
            return (long)ByteBuffer.wrap(data, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        }

        private double getDouble(byte[] data, int offset){
            return ByteBuffer.wrap(data, offset, 8).order(ByteOrder.LITTLE_ENDIAN).getDouble();
        }
}
