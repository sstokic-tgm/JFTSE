package com.jftse.server.core.codec;

import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.SerialTable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.function.BiPredicate;

public class PacketDecoder extends ByteToMessageDecoder {
    private final byte[] decryptKey;
    private int receiveIndicator = 0;
    private int header1Key = 0;

    private final Logger log;

    public PacketDecoder(int decryptKey, Logger log) {
        this.decryptKey = BitKit.getBytes(decryptKey);
        this.log = log;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int length = in.readableBytes();

        in.markReaderIndex();
        if (length < 7) {
            in.resetReaderIndex();
            return;
        }

        Packet packet;
        byte[] encryptedData = new byte[length];
        in.readBytes(encryptedData);
        byte[] data = this.decryptBytes(encryptedData, encryptedData.length);

        int currentObjectLength = BitKit.bytesToShort(data, 6);
        if (currentObjectLength < 0)
            throw new IOException("Invalid object length: " + currentObjectLength);
        if (currentObjectLength > in.capacity())
            throw new IOException("Unable to read object larger than read buffer: " + currentObjectLength);

        log.debug("payload - RECV [" + PacketOperations.getNameByValue(BitKit.bytesToShort(data, 4)) + "] " + BitKit.toString(data, 0, data.length) + ", readableBytes: " + length);

        BiPredicate<Integer, Integer> packetSizePosRangeCheck = (l, r) -> l >= r;
        while (true) {
            if (packetSizePosRangeCheck.test(6, data.length))
                throw new IOException("Invalid packet size position");

            if (!this.isValidChecksum(data)) {
                ctx.writeAndFlush(new Packet(0xFA3));
                return;
            }

            final int packetSize = BitKit.bytesToShort(data, 6);
            currentObjectLength = packetSize;
            if (packetSize + 8 < encryptedData.length) {
                final int receiveIndicator = this.receiveIndicator;
                data = decryptBytes(encryptedData, packetSize + 8);
                packet = new Packet(data);

                out.add(packet);

                // prepare data for loop
                byte[] tmp = new byte[encryptedData.length - 8 - packetSize];
                BitKit.blockCopy(encryptedData, packetSize + 8, tmp, 0, tmp.length);
                encryptedData = new byte[tmp.length];
                BitKit.blockCopy(tmp, 0, encryptedData, 0, encryptedData.length);
                data = decryptBytes(encryptedData, encryptedData.length);

                int newReceiveIndicator = receiveIndicator + 1;
                this.receiveIndicator = newReceiveIndicator % 60;
            } else {
                break;
            }
        }
        if (currentObjectLength + 8 <= encryptedData.length) {
            final int receiveIndicator = this.receiveIndicator;

            data = decryptBytes(encryptedData, currentObjectLength + 8);
            packet = new Packet(data);
            out.add(packet);

            int newReceiveIndicator = receiveIndicator + 1;
            this.receiveIndicator = newReceiveIndicator % 60;
        }
    }

    private boolean isValidChecksum(byte[] data) {
        final int receiveIndicator = this.receiveIndicator;
        final int pos = (((this.header1Key << 4) - this.header1Key * 4 + receiveIndicator) * 2);
        final short serverSerial = BitKit.bytesToShort(SerialTable.serialTable, pos);

        byte[] serverSerialData = new byte[]{BitKit.getBytes(serverSerial)[0], BitKit.getBytes(serverSerial)[1]};
        try {
            final short clientChecksum = (short) ((data[0] & 0xFF) + (data[1] & 0xFF) + (data[4] & 0xFF) + (data[5] & 0xFF) + (data[6] & 0xFF) + (data[7] & 0xFF));
            final short serverChecksum = (short) ((serverSerialData[0] & 0xFF) + (serverSerialData[1] & 0xFF) + (data[4] & 0xFF) + (data[5] & 0xFF) + (data[6] & 0xFF) + (data[7] & 0xFF));

            return clientChecksum == serverChecksum;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private byte[] decryptBytes(byte[] encryptedBuffer, int size) {
        byte[] decrypted = new byte[size];
        BitKit.blockCopy(encryptedBuffer, 0, decrypted, 0, size);

        for (int i = 0; i < size; i++)
            decrypted[i] ^= this.decryptKey[(i & 3)];

        return decrypted;
    }
}
