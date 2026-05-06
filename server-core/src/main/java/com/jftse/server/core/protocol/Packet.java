package com.jftse.server.core.protocol;

import com.jftse.emulator.common.utilities.BitKit;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Packet implements IPacket {
    private int readPosition = 0;
    private char checkSerial;
    private char checkSum;
    private char packetId;
    private char dataLength;
    private byte[] data;

    protected Packet() {
    }

    public Packet(Packet packet) {
        this.readPosition = packet.readPosition;
        this.checkSerial = packet.checkSerial;
        this.checkSum = packet.checkSum;
        this.packetId = packet.packetId;
        this.dataLength = packet.dataLength;

        this.data = new byte[this.dataLength];
        BitKit.blockCopy(packet.data, 0, this.data, 0, this.dataLength);
    }

    public Packet(byte[] rawData) {
        ByteBuffer buffer = ByteBuffer.wrap(rawData).order(ByteOrder.nativeOrder());

        this.checkSerial = buffer.getChar(0);
        this.checkSum = buffer.getChar(2);
        this.packetId = buffer.getChar(4);
        this.dataLength = buffer.getChar(6);

        this.data = new byte[this.dataLength];
        BitKit.blockCopy(rawData, 8, this.data, 0, this.dataLength);
    }

    public Packet(char packetId) {
        this.packetId = packetId;
        this.checkSerial = 0;
        this.checkSum = 0;
        this.dataLength = 0;
        this.data = new byte[16384];
    }

    public Packet(int packetId) {
        this.packetId = (char) packetId;
        this.checkSerial = 0;
        this.checkSum = 0;
        this.dataLength = 0;
        this.data = new byte[16384];
    }

    public Packet(PacketOperations packetOperation) {
        this(packetOperation.getValue());
    }

    public Packet(IPacket packet) {
        this(packet.toBytes());
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

    public void write(Object... o) {
        List<Object> dataList = new ArrayList<>(Arrays.asList(o));
        dataList.forEach(this::write);
    }

    public void write(Object element) {
        byte[] dataElement;
        if (element instanceof Character) {
            dataElement = BitKit.getBytes((char) element);
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, 2);
            this.dataLength += (char) 2;
        } else if (element instanceof Short) {
            dataElement = BitKit.getBytes((short) element);
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, 2);
            this.dataLength += (char) 2;
        } else if (element instanceof Integer) {
            dataElement = BitKit.getBytes((int) element);
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, 4);
            this.dataLength += (char) 4;
        } else if (element instanceof Long) {
            dataElement = BitKit.getBytes((long) element);
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, 8);
            this.dataLength += (char) 8;
        } else if (element instanceof String e) {
            if (!e.isEmpty()) {
                dataElement = e.getBytes(java.nio.charset.StandardCharsets.UTF_16LE);
                BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, dataElement.length);
                this.dataLength += (char) dataElement.length;
            }

            BitKit.blockCopy(new byte[]{0, 0}, 0, this.data, this.dataLength, 2);
            this.dataLength += 2;
        } else if (element instanceof Byte) {
            dataElement = BitKit.getBytes(BitKit.byteToChar((byte) element));
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, 1);
            this.dataLength += (char) 1;
        } else if (element instanceof Boolean) {
            dataElement = BitKit.getBytes((byte) ((boolean) element ? 1 : 0));
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, 1);
            this.dataLength += (char) 1;
        } else if (element instanceof Float) {
            dataElement = BitKit.getBytes((float) element);
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, 4);
            this.dataLength += (char) 4;
        } else if (element instanceof Double) {
            dataElement = BitKit.getBytes((double) element);
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, 8);
            this.dataLength += (char) 8;
        } else if (element instanceof java.util.Date) {
            dataElement = BitKit.getBytes((((java.util.Date) element).getTime() + 11644473600000L) * 10000L);
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, 8);
            this.dataLength += (char) 8;
        } else if (element instanceof byte[]) {
            dataElement = (byte[]) element;
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, dataElement.length);
            this.dataLength += (char) dataElement.length;
        } else if (element == null) {
            // do nothing for null values
        }
    }

    public void writeStringUTF8(String text) {
        if (text != null && !text.isEmpty()) {
            byte[] dataElement = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            BitKit.blockCopy(dataElement, 0, this.data, this.dataLength, dataElement.length);
            this.dataLength += (char) dataElement.length;
        }
        BitKit.blockCopy(new byte[]{0}, 0, this.data, this.dataLength, 1);
        this.dataLength += 1;
    }

    public void writeFixedString(String text, int len) {
        if (text == null || text.length() < len) return;
        byte[] dataElement = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        for (int i = 0; i < len; i++)
            write(dataElement[i]);
    }

    public <T> T read(Class<T> type) {
        Object value;
        if (type == Character.class) value = readChar();
        else if (type == Short.class) value = readShort();
        else if (type == Integer.class) value = readInt();
        else if (type == Long.class) value = readLong();
        else if (type == String.class) value = readString();
        else if (type == Byte.class) value = readByte();
        else if (type == Boolean.class) value = readBoolean();
        else if (type == Float.class) value = readFloat();
        else if (type == Double.class) value = readDouble();
        else if (type == java.util.Date.class) value = readDate();
        else value = null; // unsupported type
        return type.cast(value);
    }

    protected float readFloat() {
        float result = BitKit.bytesToFloat(this.data, this.readPosition);
        this.readPosition += 4;
        return result;
    }

    protected int readInt() {
        int result = BitKit.bytesToInt(this.data, this.readPosition);
        this.readPosition += 4;
        return result;
    }

    protected long readLong() {
        long result = BitKit.bytesToLong(this.data, this.readPosition);
        this.readPosition += 8;
        return result;
    }

    protected byte readByte() {
        byte result = this.data[this.readPosition];
        this.readPosition += 1;
        return result;
    }

    protected boolean readBoolean() {
        boolean result = this.data[this.readPosition] != 0;
        this.readPosition += 1;
        return result;
    }

    protected char readChar() {
        char element = BitKit.bytesToChar(this.data, this.readPosition);
        this.readPosition += 2;
        return element;
    }

    protected short readShort() {
        short element = BitKit.bytesToShort(this.data, this.readPosition);
        this.readPosition += 2;
        return element;
    }

    protected double readDouble() {
        double result = BitKit.bytesToDouble(this.data, this.readPosition);
        this.readPosition += 8;
        return result;
    }

    protected java.util.Date readDate() {
        long filetime = BitKit.bytesToLong(this.data, this.readPosition);
        this.readPosition += 8;
        return new java.util.Date((filetime / 10000L) - 11644473600000L);
    }

    protected <T> List<T> readRepeated(java.util.function.Supplier<T> reader) {
        int count = this.readByte();
        List<T> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(reader.get());
        }
        return list;
    }

    protected <T> List<T> readRepeated(java.util.function.Supplier<T> reader, int len) {
        List<T> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            list.add(reader.get());
        }
        return list;
    }

    protected byte[] readBytes(int len) {
        if (this.readPosition + len > this.data.length)
            len = data.length - readPosition;

        byte[] bytes = new byte[len];
        BitKit.blockCopy(this.data, this.readPosition, bytes, 0, len);
        this.readPosition += len;
        return bytes;
    }

    protected byte[] readBytes() {
        int len = this.dataLength - this.readPosition;
        return readBytes(len);
    }


    protected String readString() {
        String result = "";
        if (this.readPosition >= 0 && this.readPosition < this.data.length) {
            String text = new String(new byte[]{this.data[this.readPosition], this.data[this.readPosition + 1]});
            if (!this.isAscii(text)) {
                int stringLength = indexOf(this.data, new byte[]{0x00, 0x00}, this.readPosition) - this.readPosition;

                if (stringLength > 1) {
                    result = new String(this.data, this.readPosition, stringLength, java.nio.charset.StandardCharsets.UTF_16LE);
                    this.readPosition += stringLength + 2;
                } else {
                    this.readPosition += 2;
                }
            } else {
                int stringLength = indexOf(this.data, new byte[]{0x00}, this.readPosition) - this.readPosition;

                if (stringLength > 0) {
                    result = new String(this.data, this.readPosition, stringLength, java.nio.charset.StandardCharsets.UTF_8);
                    this.readPosition += stringLength + 1;
                } else {
                    this.readPosition += 1;
                }
            }
        }
        return result;
    }

    protected String readFixedString(int len) {
        if (this.readPosition + len > this.data.length)
            len = data.length - readPosition;

        byte[] strBytes = new byte[len];
        BitKit.blockCopy(this.data, readPosition, strBytes, 0, len);
        this.readPosition += len;

        return new String(strBytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    public long getClientTimestamp() {
        if (dataLength > 0) {
            final int oldReadPosition = readPosition;
            readPosition = dataLength - 8;

            final long clientTimestamp = readLong();

            readPosition = oldReadPosition; // reset read position, getClientTimestamp is a special case method

            return clientTimestamp;
        }
        return 0L;
    }

    public byte[] getRawPacket() {
        return this.toBytes();
    }

    public int getPacketSize() {
        return this.getRawPacket().length;
    }

    @Override
    public byte[] toBytes() {
        byte[] packet = new byte[8 + this.dataLength];
        byte[] _serial = BitKit.getBytes(this.checkSerial);
        byte[] _check = BitKit.getBytes(this.checkSum);
        byte[] _packetId = BitKit.getBytes(this.packetId);
        byte[] _dataLen = BitKit.getBytes(this.dataLength);
        BitKit.blockCopy(_serial, 0, packet, 0, 2);
        BitKit.blockCopy(_check, 0, packet, 2, 2);
        BitKit.blockCopy(_packetId, 0, packet, 4, 2);
        BitKit.blockCopy(_dataLen, 0, packet, 6, 2);
        BitKit.blockCopy(this.data, 0, packet, 8, this.dataLength);
        return packet;
    }

    @Override
    public String toString() {
        return "Packet {" +
                " \"id\": \"" + String.format("0x%X", (int) this.packetId) + "\"," +
                " \"len\": " + (int) this.dataLength + "," +
                " \"data\": " + BitKit.toString(this.data, 0, this.dataLength) + " }";
    }
}
