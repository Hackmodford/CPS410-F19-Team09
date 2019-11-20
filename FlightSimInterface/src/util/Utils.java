package util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Utils {
    public static long map(long x, long inMin, long inMax, long outMin, long outMax) {
        return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

    public static byte[] convertDoubleToByteArray(double d) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putDouble(d);
        return byteBuffer.array();
    }

    public static byte[] writeToByteArray(char cmd, double[] cmdVals){
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

    public static double getByte(byte[] data, int offset) {
        return (double) (ByteBuffer.wrap(data, offset, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xff);
    }

    public static double getShort(byte[] data, int offset) {
        return (double) ByteBuffer.wrap(data, offset, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static double getInt(byte[] data, int offset) {
        return (double) ByteBuffer.wrap(data, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static double getDouble(byte[] data, int offset) {
        return ByteBuffer.wrap(data, offset, 8).order(ByteOrder.LITTLE_ENDIAN).getDouble();
    }
}
