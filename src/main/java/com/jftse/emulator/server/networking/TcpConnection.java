package com.jftse.emulator.server.networking;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import javax.validation.ValidationException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

@Getter
@Setter
@Log4j2
public class TcpConnection {
    private SocketChannel socketChannel;
    private int keepAliveMillis = 10000;
    private final ByteBuffer readBuffer;
    private final ByteBuffer writeBuffer;
    private boolean bufferPositionFix;
    private int timeoutMillis = 12000;
    private float idleThresHold = 0.1f;

    private SelectionKey selectionKey;
    private volatile long lastWriteTime, lastReadTime;
    private int currentObjectLength;
    private final Object writeLock = new Object();

    private int header1Key = 0;
    private int sendIndicator = 0;
    private int receiveIndicator = 0;
    private final byte[] decryptKey;
    private final byte[] encryptKey;
    private final byte[] serialTable = {
            (byte) 0xF2, (byte) 0x30, (byte) 0x75, (byte) 0x86, (byte) 0xD4, (byte) 0x7D, (byte) 0x57, (byte) 0x38, (byte) 0x6E, (byte) 0x68,
            (byte) 0x4F, (byte) 0x7E, (byte) 0x30, (byte) 0x58, (byte) 0xED, (byte) 0x7D, (byte) 0x5C, (byte) 0x47, (byte) 0xC3, (byte) 0x31,
            (byte) 0xCA, (byte) 0x2B, (byte) 0x5F, (byte) 0x56, (byte) 0xC8, (byte) 0x7A, (byte) 0x65, (byte) 0x34, (byte) 0xF6, (byte) 0x62,
            (byte) 0x31, (byte) 0x5B, (byte) 0x00, (byte) 0x38, (byte) 0x15, (byte) 0x5B, (byte) 0xD8, (byte) 0x2F, (byte) 0xA7, (byte) 0x57,
            (byte) 0xB8, (byte) 0x79, (byte) 0x3D, (byte) 0x3C, (byte) 0x40, (byte) 0x6C, (byte) 0xFB, (byte) 0x89, (byte) 0xBE, (byte) 0x63,
            (byte) 0x19, (byte) 0x5F, (byte) 0x36, (byte) 0x57, (byte) 0xC1, (byte) 0x81, (byte) 0xEC, (byte) 0x52, (byte) 0x15, (byte) 0x58,
            (byte) 0x2A, (byte) 0x35, (byte) 0x3B, (byte) 0x7F, (byte) 0x6A, (byte) 0x7E, (byte) 0xF9, (byte) 0x40, (byte) 0x44, (byte) 0x7E,
            (byte) 0xF7, (byte) 0x3F, (byte) 0xD8, (byte) 0x6E, (byte) 0xA5, (byte) 0x57, (byte) 0xA8, (byte) 0x2D, (byte) 0x43, (byte) 0x57,
            (byte) 0xC2, (byte) 0x56, (byte) 0x4D, (byte) 0x63, (byte) 0xF4, (byte) 0xCB, (byte) 0xBD, (byte) 0x81, (byte) 0x4E, (byte) 0x7E,
            (byte) 0xB5, (byte) 0x5E, (byte) 0x1A, (byte) 0x5F, (byte) 0xB1, (byte) 0x5A, (byte) 0x8A, (byte) 0x37, (byte) 0xB5, (byte) 0x53,
            (byte) 0x14, (byte) 0xA5, (byte) 0xEB, (byte) 0x56, (byte) 0x5B, (byte) 0x60, (byte) 0xD1, (byte) 0x63, (byte) 0x70, (byte) 0x57,
            (byte) 0xF5, (byte) 0x64, (byte) 0xC6, (byte) 0xAD, (byte) 0xD7, (byte) 0x57, (byte) 0xCC, (byte) 0x5E, (byte) 0x2D, (byte) 0x31,
            (byte) 0x04, (byte) 0x7E, (byte) 0xEB, (byte) 0x56, (byte) 0xE7, (byte) 0x38, (byte) 0xE5, (byte) 0x63, (byte) 0xD4, (byte) 0x57,
            (byte) 0x3D, (byte) 0x59, (byte) 0x96, (byte) 0x38, (byte) 0x77, (byte) 0x67, (byte) 0xC0, (byte) 0x60, (byte) 0x2D, (byte) 0x31,
            (byte) 0x1A, (byte) 0xD1, (byte) 0xD9, (byte) 0x86, (byte) 0xDE, (byte) 0x7D, (byte) 0x07, (byte) 0x4C, (byte) 0xCE, (byte) 0x58,
            (byte) 0x87, (byte) 0x7D, (byte) 0x08, (byte) 0x58, (byte) 0xD9, (byte) 0x7D, (byte) 0x04, (byte) 0x2C, (byte) 0xCF, (byte) 0x2F,
            (byte) 0x16, (byte) 0x7B, (byte) 0xB7, (byte) 0x58, (byte) 0xFA, (byte) 0x7A, (byte) 0x45, (byte) 0x40, (byte) 0xEA, (byte) 0x64,
            (byte) 0x73, (byte) 0x82, (byte) 0x46, (byte) 0x5B, (byte) 0x79, (byte) 0x5B, (byte) 0xC0, (byte) 0x7E, (byte) 0xC5, (byte) 0x57,
            (byte) 0x58, (byte) 0x89, (byte) 0x69, (byte) 0x3D, (byte) 0x86, (byte) 0x6C, (byte) 0xB5, (byte) 0x89, (byte) 0x2E, (byte) 0x62,
            (byte) 0xE9, (byte) 0x66, (byte) 0x66, (byte) 0x59, (byte) 0xDF, (byte) 0x81, (byte) 0xB4, (byte) 0x53, (byte) 0xCD, (byte) 0x63,
            (byte) 0xDC, (byte) 0x7D, (byte) 0x8B, (byte) 0x57, (byte) 0x84, (byte) 0x91, (byte) 0xE9, (byte) 0x5A, (byte) 0x60, (byte) 0x30,
            (byte) 0xB1, (byte) 0x67, (byte) 0x0A, (byte) 0x38, (byte) 0x81, (byte) 0x62, (byte) 0x72, (byte) 0x3B, (byte) 0x55, (byte) 0x63,
            (byte) 0x62, (byte) 0x34, (byte) 0x31, (byte) 0x7F, (byte) 0x38, (byte) 0x7E, (byte) 0x59, (byte) 0x31, (byte) 0xCC, (byte) 0x91,
            (byte) 0xBF, (byte) 0x40, (byte) 0xE2, (byte) 0x6E, (byte) 0xD7, (byte) 0x57, (byte) 0xE0, (byte) 0x2C, (byte) 0x2B, (byte) 0x5B,
            (byte) 0x04, (byte) 0x7E, (byte) 0xBD, (byte) 0x57, (byte) 0x84, (byte) 0x91, (byte) 0x79, (byte) 0x5C, (byte) 0x8C, (byte) 0x31,
            (byte) 0xC5, (byte) 0x67, (byte) 0x1E, (byte) 0x38, (byte) 0xB3, (byte) 0x62, (byte) 0x02, (byte) 0x3D, (byte) 0x3D, (byte) 0x67,
            (byte) 0x62, (byte) 0x34, (byte) 0x31, (byte) 0x7F, (byte) 0x38, (byte) 0x7E, (byte) 0x77, (byte) 0x31, (byte) 0xCC, (byte) 0x91,
            (byte) 0x23, (byte) 0x41, (byte) 0x0E, (byte) 0x70, (byte) 0x9F, (byte) 0x58, (byte) 0xA8, (byte) 0x2D, (byte) 0x49, (byte) 0x5B,
            (byte) 0x2E, (byte) 0xD1, (byte) 0x21, (byte) 0x85, (byte) 0x9E, (byte) 0x77, (byte) 0xD7, (byte) 0x3A, (byte) 0xB0, (byte) 0x58,
            (byte) 0xB7, (byte) 0x75, (byte) 0x08, (byte) 0x58, (byte) 0xD9, (byte) 0x7D, (byte) 0x04, (byte) 0x2C, (byte) 0xCF, (byte) 0x2F,
            (byte) 0x94, (byte) 0x7F, (byte) 0x7B, (byte) 0x58, (byte) 0xE7, (byte) 0x38, (byte) 0xDB, (byte) 0x63, (byte) 0xC0, (byte) 0x57,
            (byte) 0x75, (byte) 0x58, (byte) 0x66, (byte) 0x40, (byte) 0x3F, (byte) 0x68, (byte) 0x5C, (byte) 0x60, (byte) 0xCD, (byte) 0x40,
            (byte) 0x5E, (byte) 0x89, (byte) 0x6E, (byte) 0x3D, (byte) 0x72, (byte) 0x6C, (byte) 0xBF, (byte) 0x89, (byte) 0x22, (byte) 0x64,
            (byte) 0xE9, (byte) 0x66, (byte) 0x06, (byte) 0x69, (byte) 0x97, (byte) 0x8D, (byte) 0x7C, (byte) 0x54, (byte) 0x31, (byte) 0x64,
            (byte) 0x02, (byte) 0x2B, (byte) 0x6B, (byte) 0x68, (byte) 0xC2, (byte) 0x7B, (byte) 0xEF, (byte) 0x40, (byte) 0x36, (byte) 0x64,
            (byte) 0x4B, (byte) 0x87, (byte) 0x68, (byte) 0x5C, (byte) 0x77, (byte) 0x67, (byte) 0x64, (byte) 0x8F, (byte) 0xCD, (byte) 0x4F,
            (byte) 0x8E, (byte) 0x35, (byte) 0xF9, (byte) 0x7F, (byte) 0x74, (byte) 0x7E, (byte) 0x81, (byte) 0x31, (byte) 0xFE, (byte) 0x91,
            (byte) 0x87, (byte) 0x41, (byte) 0x72, (byte) 0x70, (byte) 0x03, (byte) 0x59, (byte) 0xD6, (byte) 0x2C, (byte) 0x53, (byte) 0x5B,
            (byte) 0xF6, (byte) 0xD1, (byte) 0xDD, (byte) 0x87, (byte) 0xD0, (byte) 0x77, (byte) 0xE1, (byte) 0x3A, (byte) 0xF6, (byte) 0x58,
            (byte) 0xDF, (byte) 0x75, (byte) 0xD0, (byte) 0x58, (byte) 0x15, (byte) 0x7E, (byte) 0xD2, (byte) 0x2B, (byte) 0xF7, (byte) 0x2F,
            (byte) 0x5E, (byte) 0x79, (byte) 0xFB, (byte) 0x50, (byte) 0x26, (byte) 0x7C, (byte) 0x59, (byte) 0x40, (byte) 0x56, (byte) 0x62,
            (byte) 0xAF, (byte) 0x87, (byte) 0xCC, (byte) 0x5C, (byte) 0xB5, (byte) 0x5B, (byte) 0xC3, (byte) 0x7F, (byte) 0xF9, (byte) 0x50,
            (byte) 0x00, (byte) 0x3C, (byte) 0xDD, (byte) 0x2D, (byte) 0xFA, (byte) 0x34, (byte) 0x91, (byte) 0x5D, (byte) 0xC4, (byte) 0x30,
            (byte) 0xE1, (byte) 0x5F, (byte) 0x6E, (byte) 0x38, (byte) 0x49, (byte) 0x63, (byte) 0xBA, (byte) 0x2F, (byte) 0x85, (byte) 0xD8,
            (byte) 0x1E, (byte) 0x7A, (byte) 0xED, (byte) 0x56, (byte) 0xB5, (byte) 0x38, (byte) 0xB3, (byte) 0x63, (byte) 0xCA, (byte) 0x57,
            (byte) 0x49, (byte) 0x57, (byte) 0x32, (byte) 0x38, (byte) 0xE3, (byte) 0x5A, (byte) 0x90, (byte) 0x54, (byte) 0x15, (byte) 0x67,
            (byte) 0x62, (byte) 0x89, (byte) 0x9B, (byte) 0x3D, (byte) 0x7C, (byte) 0x6C, (byte) 0x8D, (byte) 0x89, (byte) 0x8E, (byte) 0x52,
            (byte) 0x21, (byte) 0x66, (byte) 0xD6, (byte) 0x57, (byte) 0xDF, (byte) 0x81, (byte) 0xB4, (byte) 0x53, (byte) 0x2D, (byte) 0x54,
            (byte) 0x62, (byte) 0x34, (byte) 0x1B, (byte) 0x30, (byte) 0x38, (byte) 0x7E, (byte) 0x45, (byte) 0x31, (byte) 0xCC, (byte) 0x91,
            (byte) 0x83, (byte) 0x40, (byte) 0x8A, (byte) 0x53, (byte) 0xAB, (byte) 0x56, (byte) 0x9A, (byte) 0x2C, (byte) 0x17, (byte) 0x5B,
            (byte) 0x12, (byte) 0x52, (byte) 0xFB, (byte) 0x50, (byte) 0xCE, (byte) 0x79, (byte) 0x27, (byte) 0x40, (byte) 0x42, (byte) 0x62,
            (byte) 0x1F, (byte) 0x86, (byte) 0x10, (byte) 0x5A, (byte) 0xBF, (byte) 0x5B, (byte) 0xDC, (byte) 0x7B, (byte) 0xCD, (byte) 0x4F,
            (byte) 0xDC, (byte) 0x7D, (byte) 0x8B, (byte) 0x57, (byte) 0x84, (byte) 0x91, (byte) 0xB9, (byte) 0x62, (byte) 0x28, (byte) 0x31,
            (byte) 0x15, (byte) 0x68, (byte) 0x39, (byte) 0x30, (byte) 0xB1, (byte) 0x5A, (byte) 0x39, (byte) 0x30, (byte) 0x75, (byte) 0x34,
            (byte) 0x2E, (byte) 0xD1, (byte) 0xEF, (byte) 0x84, (byte) 0x9E, (byte) 0x77, (byte) 0xD7, (byte) 0x3A, (byte) 0x20, (byte) 0x57,
            (byte) 0xB7, (byte) 0x75, (byte) 0x78, (byte) 0x56, (byte) 0xD9, (byte) 0x7D, (byte) 0x04, (byte) 0x2C, (byte) 0x6B, (byte) 0x2F,
            (byte) 0xE4, (byte) 0x2F, (byte) 0x49, (byte) 0x57, (byte) 0xF7, (byte) 0x5F, (byte) 0x2D, (byte) 0x58, (byte) 0x5C, (byte) 0x6B,
            (byte) 0x25, (byte) 0x5D, (byte) 0x96, (byte) 0x38, (byte) 0x67, (byte) 0x40, (byte) 0x8E, (byte) 0x60, (byte) 0x19, (byte) 0x31,
            (byte) 0x1B, (byte) 0x30, (byte) 0xC9, (byte) 0x2D, (byte) 0x76, (byte) 0x45, (byte) 0x15, (byte) 0x7A, (byte) 0x2E, (byte) 0x62,
            (byte) 0x61, (byte) 0x53, (byte) 0xAA, (byte) 0x56, (byte) 0x9B, (byte) 0x84, (byte) 0x3C, (byte) 0x67, (byte) 0xCD, (byte) 0x63,
            (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
    };

    public TcpConnection(int writeBufferSize, int readBufferSize, int decKey, int encKey) {
        this.writeBuffer = ByteBuffer.allocate(writeBufferSize);
        this.readBuffer = ByteBuffer.allocate(readBufferSize);

        writeBuffer.order(ByteOrder.LITTLE_ENDIAN);
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);

        this.decryptKey = BitKit.getBytes(decKey);
        this.encryptKey = BitKit.getBytes(encKey);
    }

    public SelectionKey accept(Selector selector, SocketChannel socketChannel) throws IOException {
        writeBuffer.clear();
        readBuffer.clear();
        currentObjectLength = 0;

        try {
            this.socketChannel = socketChannel;
            socketChannel.configureBlocking(false);
            Socket socket = socketChannel.socket();
            socket.setOption(StandardSocketOptions.TCP_NODELAY, true);
            socket.setOption(StandardSocketOptions.SO_REUSEADDR, true);

            selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);

            lastReadTime = lastWriteTime = System.currentTimeMillis();

            return selectionKey;
        } catch (IOException ioe) {
            close();
            throw ioe;
        }
    }

    public List<Packet> readPacket() throws IOException {
        readBuffer.clear();

        SocketChannel socketChannel = this.socketChannel;
        if (socketChannel == null || !socketChannel.isConnected())
            throw new SocketException("Connection is closed.");

        int bytesRead = -1;
        try {
            bytesRead = socketChannel.read(readBuffer);
        } catch (IOException ioe) {
            throw new IOException("Exception occurred while reading from socket channel: " + ioe.getMessage());
        }
        readBuffer.flip();

        lastReadTime = System.currentTimeMillis();

        if (bytesRead == -1)
            throw new SocketException("Socket channel reached EOF.");

        if (bytesRead < 7)
            return null;

        List<Packet> result = new ArrayList<>();

        Packet packet;
        byte[] encryptedData = new byte[bytesRead];
        BitKit.blockCopy(readBuffer.array(), 0, encryptedData, 0, bytesRead);
        byte[] data = decryptBytes(encryptedData, encryptedData.length);

        currentObjectLength = BitKit.bytesToShort(data, 6);
        if (currentObjectLength < 0)
            throw new IOException("Invalid object length: " + currentObjectLength);
        if (currentObjectLength > readBuffer.capacity())
            throw new IOException("Unable to read object larger than read buffer: " + currentObjectLength);

        if (ConfigService.getInstance().getValue("logging.packets.all.enabled", true))
            log.debug("payload - RECV " + BitKit.toString(encryptedData, 0, encryptedData.length) + " bytesRead: " + bytesRead);

        BiPredicate<Integer, Integer> packetSizePosRangeCheck = (l, r) -> l >= r;
        // a read tcp packet may contain multiple nested packets, so we handle that properly
        while (true) {
            if (packetSizePosRangeCheck.test(6, data.length))
                throw new IOException("Invalid packet size position");

            if (!this.isValidChecksum(data)) {
                log.error("Invalid packet header lets try not to disconnect him...");
                try {

                    this.send(new Packet((char) 0xFA3));
                } catch (IOException ioe) {
                    throw new IOException("Remote disconnected. This was caused by the last received invalid packet header");
                }
                log.debug("Should be still connected, don't disconnect him");
                return null;
            }

            int packetSize = BitKit.bytesToShort(data, 6);
            currentObjectLength = packetSize;

            if (packetSize + 8 < encryptedData.length) {
                data = decryptBytes(encryptedData, packetSize + 8);
                packet = new Packet(data);

                if (ConfigService.getInstance().getValue("logging.packets.all.enabled", true))
                    log.info("RECV [" + (ConfigService.getInstance().getValue("packets.id.translate.enabled", true) ? PacketOperations.getNameByValue(packet.getPacketId()) : String.format("0x%X", (int) packet.getPacketId())) + "] " + BitKit.toString(packet.getRawPacket(), 0, packet.getDataLength() + 8));

                result.add(packet);

                // prepare data for loop
                byte[] tmp = new byte[encryptedData.length - 8 - packetSize];
                BitKit.blockCopy(encryptedData, packetSize + 8, tmp, 0, tmp.length);
                encryptedData = new byte[tmp.length];
                BitKit.blockCopy(tmp, 0, encryptedData, 0, encryptedData.length);
                data = decryptBytes(encryptedData, encryptedData.length);

                this.receiveIndicator++;
                this.receiveIndicator %= 60;
            } else {
                break;
            }
        }
        if (currentObjectLength + 8 <= encryptedData.length) {
            data = decryptBytes(encryptedData, currentObjectLength + 8);
            packet = new Packet(data);
            this.receiveIndicator++;
            this.receiveIndicator %= 60;

            if (ConfigService.getInstance().getValue("logging.packets.all.enabled", true))
                log.info("RECV [" + (ConfigService.getInstance().getValue("packets.id.translate.enabled", true) ? PacketOperations.getNameByValue(packet.getPacketId()) : String.format("0x%X", (int) packet.getPacketId())) + "] " + BitKit.toString(packet.getRawPacket(), 0, packet.getDataLength() + 8));
        } else {
            packet = null;
        }
        currentObjectLength = 0;

        if (packet != null)
            result.add(packet);

        return result;
    }

    public void writeOperation() throws IOException {
        synchronized (writeLock) {
            if (writeToSocket()) {
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
            lastWriteTime = System.currentTimeMillis();
        }
    }

    private boolean writeToSocket() throws IOException {
        SocketChannel socketChannel = this.socketChannel;
        if (socketChannel == null || !socketChannel.isConnected())
            throw new SocketException("Connection is closed.");

        ByteBuffer buffer = writeBuffer;
        buffer.flip();
        while (buffer.hasRemaining()) {

            if (socketChannel.write(buffer) == 0)
                break;
        }
        buffer.compact();

        return buffer.position() == 0;
    }

    public int send(Packet... packets) throws IOException {
        SocketChannel socketChannel = this.socketChannel;
        if (socketChannel == null || !socketChannel.isConnected())
            throw new SocketException("Connection is closed.");

        synchronized (writeLock) {
            int length = packets.length;
            for (Packet packet : packets)
                length += (packet.getDataLength() + 8);

            if (length > 16384)
                throw new ValidationException("Packet length can not be bigger than 16384");

            writeBuffer.clear();

            for (Packet packet : packets) {
                byte[] data = packet.getRawPacket();
                byte[] encryptedData;
                createSerial(data);
                createCheckSum(data);
                if (packet.getPacketId() != PacketOperations.S2CLoginWelcomePacket.getValueAsChar()) {
                    encryptedData = encryptBytes(data, data.length);
                    writeBuffer.put(encryptedData);
                } else {
                    writeBuffer.put(data);
                }
                if (ConfigService.getInstance().getValue("logging.packets.all.enabled", true))
                    log.info("SEND [" + (ConfigService.getInstance().getValue("packets.id.translate.enabled", true) ? PacketOperations.getNameByValue(packet.getPacketId()) : String.format("0x%X", (int) packet.getPacketId())) + "] " + BitKit.toString(packet.getRawPacket(), 0, packet.getDataLength() + 8));
            }

            if (!writeToSocket()) {
                selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            } else {
                selectionKey.selector().wakeup();
            }

            lastWriteTime = System.currentTimeMillis();

            return length;
        }
    }

    private byte[] decryptBytes(byte[] encryptedBuffer, int size) {
        byte[] decrypted = new byte[size];
        BitKit.blockCopy(encryptedBuffer, 0, decrypted, 0, size);

        for (int i = 0; i < size; i++)
            decrypted[i] ^= this.decryptKey[(i & 3)];

        return decrypted;
    }

    private byte[] encryptBytes(byte[] decryptedBuffer, int size) {
        byte[] encrypted = new byte[size];
        BitKit.blockCopy(decryptedBuffer, 0, encrypted, 0, size);

        for (int i = 0; i < size; i++)
            encrypted[i] ^= this.encryptKey[(i & 3)];

        return encrypted;
    }

    private void createSerial(byte[] data) {
        int pos = (((this.header1Key << 4) - this.header1Key * 4 + this.sendIndicator) * 2);
        short header = BitKit.bytesToShort(serialTable, pos);

        data[0] = BitKit.getBytes(header)[0];
        data[1] = BitKit.getBytes(header)[1];

        this.sendIndicator += 1;
        this.sendIndicator %= 60;
    }

    private boolean isValidChecksum(byte[] data) {
        int pos = (((this.header1Key << 4) - this.header1Key * 4 + this.receiveIndicator) * 2);
        short serverSerial = BitKit.bytesToShort(serialTable, pos);

        byte[] serverSerialData = new byte[]{BitKit.getBytes(serverSerial)[0], BitKit.getBytes(serverSerial)[1]};
        try {
            short clientChecksum = (short) ((data[0] & 0xFF) + (data[1] & 0xFF) + (data[4] & 0xFF) + (data[5] & 0xFF) + (data[6] & 0xFF) + (data[7] & 0xFF));
            short serverChecksum = (short) ((serverSerialData[0] & 0xFF) + (serverSerialData[1] & 0xFF) + (data[4] & 0xFF) + (data[5] & 0xFF) + (data[6] & 0xFF) + (data[7] & 0xFF));

            return clientChecksum == serverChecksum;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private void createCheckSum(byte[] data) {
        short checksum = (short) ((data[0] & 0xFF) + (data[1] & 0xFF) + (data[4] & 0xFF) + (data[5] & 0xFF) + (data[6] & 0xFF) + (data[7] & 0xFF));

        if (checksum % 2 == 0) {
            checksum += (short) 1587;

            data[2] = BitKit.getBytes(checksum)[0];
            data[3] = BitKit.getBytes(checksum)[1];
        } else {
            checksum += (short) 1568;

            data[2] = BitKit.getBytes(checksum)[0];
            data[3] = BitKit.getBytes(checksum)[1];
        }
    }

    public void close() {
        try {
            if (socketChannel != null) {
                socketChannel.socket().shutdownInput();
                socketChannel.socket().shutdownOutput();
                socketChannel.close();
                if (selectionKey != null)
                    selectionKey.selector().wakeup();
            }
        } catch (IOException ioe) {
            log.error("Couldn't close socket channel.", ioe);
        } finally {
            try {
                if (socketChannel != null) {
                    socketChannel.close();
                    if (selectionKey != null)
                        selectionKey.selector().wakeup();
                }
            } catch (IOException ioe) {
                log.error("Failed to close socket channel in finally block.", ioe);
            }
        }
        socketChannel = null;
    }

    public boolean needsKeepAlive(long time) {
        return socketChannel != null && keepAliveMillis > 0 && time - lastWriteTime > keepAliveMillis;
    }

    public boolean isTimedOut(long time) {
        return socketChannel != null && timeoutMillis > 0 && time - lastReadTime > timeoutMillis;
    }
}
