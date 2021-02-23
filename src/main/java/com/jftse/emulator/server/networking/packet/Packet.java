package com.jftse.emulator.server.networking.packet;

import com.jftse.emulator.common.utilities.BitKit;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class Packet {
    private int readPosition = 0;
    private char checkSerial;
    private char checkSum;
    private char packetId;
    private char dataLength;
    private byte[] data;

    public Packet(Packet packet) {
        this.checkSerial = packet.checkSerial;
        this.checkSum = packet.checkSum;
        this.packetId = packet.packetId;
        this.dataLength = packet.dataLength;

        this.data = new byte[this.dataLength];
        BitKit.blockCopy(packet.data, 0, this.data, 0, this.dataLength);
    }

    public Packet(byte[] rawData) {
        this.checkSerial = BitKit.bytesToChar(rawData, 0);
        this.checkSum = BitKit.bytesToChar(rawData, 2);
        this.packetId = BitKit.bytesToChar(rawData, 4);
        this.dataLength = BitKit.bytesToChar(rawData, 6);

        this.data = new byte[this.dataLength];
        BitKit.blockCopy(rawData, 8, this.data, 0, this.dataLength);
    }

    public Packet(char packetId) {
        this.packetId = packetId;
        this.dataLength = 0;
        this.data = new byte[4096];
    }

    private int indexOf(byte[] data, byte[] pattern, int offset) {
        for (int i = offset; i < data.length; i += pattern.length) {
            boolean found = false;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j])
                    break;
                found = true;
            }
            if (found)
                return i;
        }
        return -1;
    }

    private boolean isAscii(String text) {
        return text.chars().allMatch(c -> c >= 0x20 && c < 0x7F);
    }

    public void write(Object... data) {
        List<Object> dataList = new ArrayList<>(Arrays.asList(data));
        dataList.forEach(this::write);
    }

    public void write(Object element) {
        byte[] dataElement;
        if (element instanceof Character) {
            dataElement = BitKit.getBytes((char)element);
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, 2);
            this.dataLength += (char)2;
        }
        else if (element instanceof Short) {
            dataElement = BitKit.getBytes((short)element);
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, 2);
            this.dataLength += (char)2;
        }
        else if (element instanceof Integer) {
            dataElement = BitKit.getBytes((int)element);
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, 4);
            this.dataLength += (char)4;
        }
        else if (element instanceof Long) {
            dataElement = BitKit.getBytes((long)element);
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, 8);
            this.dataLength += (char)8;
        }
        else if (element instanceof String) {
            dataElement = element.toString().getBytes(StandardCharsets.UTF_16LE);
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, dataElement.length);
            this.dataLength += (char)dataElement.length;

            BitKit.blockCopy(new byte[] { 0, 0 }, 0, this.data, this.dataLength, 2);
            this.dataLength += 2;
        }
        else if (element instanceof Byte) {
            dataElement = BitKit.getBytes(BitKit.byteToChar((byte)element));
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, 1);
            this.dataLength += (char)1;
        }
        else if (element instanceof Boolean) {
            dataElement = BitKit.getBytes((byte)((boolean)element ? 1 : 0));
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, 1);
            this.dataLength += (char)1;
        }
        else if (element instanceof Float) {
            dataElement = BitKit.getBytes((float)element);
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, 4);
            this.dataLength += (char)4;
        }
    }

    public byte[] addByteToArray(byte[] byteArray, byte newByte) {
        byte[] newArray = new byte[byteArray.length + 1];
        BitKit.blockCopy(byteArray, 0, newArray, 1, newArray.length);
        newArray[0] = newByte;
        return newArray;
    }

    public float readFloat() {
        float result = BitKit.bytesToFloat(this.data, readPosition);
        this.readPosition += 4;
        return result;
    }

    public int readInt() {
        int result = BitKit.bytesToInt(this.data, readPosition);
        this.readPosition += 4;
        return result;
    }

    public byte readByte() {
        byte result = this.data[this.readPosition];
        this.readPosition += 1;
        return result;
    }

    public void readByte(byte element) {
        element = this.data[this.readPosition];
        this.readPosition += 1;
    }

    public char readChar() {
        char element = BitKit.bytesToChar(this.data, readPosition);
        this.readPosition += 2;
        return element;
    }

    public short readShort() {
        short element = BitKit.bytesToShort(this.data, readPosition);
        this.readPosition += 2;
        return element;
    }

    public String readUnicodeString() {
        String result = "";
        if (this.readPosition >= 0 && this.readPosition < this.data.length) {
            String text = new String(new byte[]{this.data[this.readPosition], this.data[this.readPosition + 1]});
            if (!this.isAscii(text)) {
                int stringLength = indexOf(this.data, new byte[]{0x00, 0x00}, this.readPosition) - this.readPosition;

                if (stringLength > 1) {
                    result = new String(this.data, this.readPosition, stringLength, StandardCharsets.UTF_16LE);
                    this.readPosition += stringLength + 2;
                } else {
                    this.readPosition += 2;
                }
            }
            else {
                result = this.readString();
            }
        }

        return result;
    }

    public String readString() {
        String result = "";
        int stringLength = indexOf(this.data, new byte[] {0x00}, this.readPosition) - this.readPosition;

        if(stringLength > 0) {
            result = new String(this.data, this.readPosition, stringLength, StandardCharsets.US_ASCII);
            this.readPosition += stringLength + 1;
        }

        return result;
    }

    public byte[] getRawPacket() {
        byte[] packet = new byte[8 + this.dataLength];

        byte[] serial = BitKit.getBytes(this.checkSerial);
        byte[] check = BitKit.getBytes(this.checkSum);
        byte[] packetId = BitKit.getBytes(this.packetId);
        byte[] dataLength = BitKit.getBytes(this.dataLength);

        BitKit.blockCopy(serial, 0, packet, 0, 2);
        BitKit.blockCopy(check, 0, packet, 2, 2);
        BitKit.blockCopy(packetId, 0, packet, 4, 2);
        BitKit.blockCopy(dataLength, 0, packet, 6, 2);
        BitKit.blockCopy(this.data, 0, packet, 8, this.dataLength);

        return packet;
    }
}
