package com.jftse.server.core.protocol;

import com.jftse.emulator.common.utilities.BitKit;

public interface IPacket {
    byte[] toBytes();
    char getDataLength();
    char getPacketId();
    char getCheckSerial();
    char getCheckSum();
    String toString();

    default byte[] addByteToArray(byte[] byteArray, byte newByte) {
        byte[] newArray = new byte[byteArray.length + 1];
        BitKit.blockCopy(byteArray, 0, newArray, 1, newArray.length);
        newArray[0] = newByte;
        return newArray;
    }

    default byte[] addBytesToArray(byte[] byteArray, byte[] newBytes) {
        byte[] newArray = new byte[byteArray.length + newBytes.length];
        BitKit.blockCopy(byteArray, 0, newArray, newBytes.length, byteArray.length);
        BitKit.blockCopy(newBytes, 0, newArray, 0, newBytes.length);
        return newArray;
    }
}
