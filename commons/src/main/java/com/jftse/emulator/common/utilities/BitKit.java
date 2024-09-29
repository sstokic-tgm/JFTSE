package com.jftse.emulator.common.utilities;

import io.netty.buffer.ByteBuf;

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
        return (char)((bytes[index] & 0xFF) | ((bytes[index + 1] & 0xFF) << 8));
    }

    public static short bytesToShort(byte[] bytes, int index) {
        return (short)((bytes[index] & 0xFF) | ((bytes[index + 1] & 0xFF) << 8));
    }

    public static int bytesToInt(byte[] bytes, int index) {
        return (bytes[index] & 0xFF) | ((bytes[index + 1] & 0xFF) << 8) | ((bytes[index + 2] & 0xFF) << 16) | ((bytes[index + 3] & 0xFF) << 24);
    }

    public static float bytesToFloat(byte[] bytes, int index) {
        return Float.intBitsToFloat(bytesToInt(bytes, index));
    }

    public static long bytesToLong(byte[] bytes, int index) {
        return (bytes[index] & 0xFFL) | ((bytes[index + 1] & 0xFFL) << 8) | ((bytes[index + 2] & 0xFFL) << 16) | ((bytes[index + 3] & 0xFFL) << 24) | ((bytes[index + 4] & 0xFFL) << 32) | ((bytes[index + 5] & 0xFFL) << 40) | ((bytes[index + 6] & 0xFFL) << 48) | ((bytes[index + 7] & 0xFFL) << 56);
    }

    public static byte[] getBytes(byte value) {
        return new byte[] { value };
    }

    public static byte[] getBytes(char value) {
        return new byte[] { (byte)value, (byte)(value >> 8) };
    }

    public static byte[] getBytes(int value) {
        return new byte[] { (byte)value, (byte)(value >> 8), (byte)(value >> 16), (byte)(value >> 24) };
    }

    public static byte[] getBytes(float value) {
        return getBytes(Float.floatToRawIntBits(value));
    }

    public static byte[] getBytes(long value) {
        return new byte[] { (byte)value, (byte)(value >> 8), (byte)(value >> 16), (byte)(value >> 24), (byte)(value >> 32), (byte)(value >> 40), (byte)(value >> 48), (byte)(value >> 56) };
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

    public static String toString(ByteBuf buf, int startIndex, int length) {
        StringBuilder sb = new StringBuilder();
        buf.forEachByte(startIndex, length, (byte b) -> {
            sb.append(DIGITS_UPPER[(0xF0 & b) >>> 4]);
            sb.append(DIGITS_UPPER[0x0F & b]);
            sb.append(' ');
            return true;
        });
        return sb.toString();
    }
}
