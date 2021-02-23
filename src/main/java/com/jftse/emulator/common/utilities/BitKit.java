package com.jftse.emulator.common.utilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BitKit {
    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static byte fromUnsignedInt(int value) {
        return (byte)value;
    }

    public static int toUnsignedInt(byte value) {
        return (value & 0x7F) + (value < 0 ? 128 : 0);
    }

    public static char byteToChar(byte value) {
        return (char)toUnsignedInt(value);
    }

    public static char bytesToChar(byte[] bytes, int index) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder()).getChar(index);
    }

    public static short bytesToShort(byte[] bytes, int index) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder()).getShort(index);
    }

    public static int bytesToInt(byte[] bytes, int index) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder()).getInt(index);
    }

    public static float bytesToFloat(byte[] bytes, int index) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder()).getFloat(index);
    }

    public static byte[] getBytes(byte value) {
        return ByteBuffer.allocate(1).order(ByteOrder.nativeOrder()).put(value).array();
    }

    public static byte[] getBytes(char value) {
        return ByteBuffer.allocate(2).order(ByteOrder.nativeOrder()).putChar(value).array();
    }

    public static byte[] getBytes(int value) {
        return ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(value).array();
    }

    public static byte[] getBytes(float value) {
        return ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putFloat(value).array();
    }

    public static byte[] getBytes(long value) {
        return ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putLong(value).array();
    }

    public static void blockCopy(Object src, int srcPos, Object dest, int destPos, int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }

    public static String toString(byte[] value, int startIndex, int length) {
        int chArrayLength = length * 3;

        char[] chArray = new char[chArrayLength];
        int index = startIndex;

        for(int i = 0; i < chArrayLength; i += 3) {

            byte b = value[index++];
            chArray[i] = DIGITS_UPPER[(0xF0 & b) >>> 4];
            chArray[i + 1] = DIGITS_UPPER[0x0F & b];
            chArray[i + 2] = ' ';
        }

        return new String(chArray, 0, chArray.length - 1);
    }
}
