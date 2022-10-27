package com.jftse.server.core.codec;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.SerialTable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.Logger;

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
        int actualReaderIndex = in.readerIndex();

        boolean logAllPackets = ConfigService.getInstance().getValue("logging.packets.all.enabled", true);

        Packet packet;
        byte[] encryptedData = new byte[length];
        in.readBytes(encryptedData);
        byte[] data = decryptBytes(encryptedData, encryptedData.length);

        if (logAllPackets)
            log.debug("RECV payload " + BitKit.toString(data, 0, data.length) + ", readableBytes: " + length);

        if (!this.isValidChecksum(data)) {
            log.error("Invalid packet checksum");
            ctx.writeAndFlush(new Packet(0xFA3));
            in.resetReaderIndex();
            return;
        }

        int currentObjectLength = BitKit.bytesToShort(data, 6);
        if (currentObjectLength < 0) {
            log.error("Invalid object length: " + currentObjectLength);
            in.resetReaderIndex();
            return;
        }
        if (currentObjectLength > in.capacity()) {
            log.error("Unable to read object larger than read buffer: " + currentObjectLength);
            in.resetReaderIndex();
            return;
        }

        BiPredicate<Integer, Integer> packetSizePosRangeCheck = (l, r) -> l >= r;
        while (true) {
            if (packetSizePosRangeCheck.test(6, data.length)) {
                log.error("Invalid packet size position");
                in.resetReaderIndex();
                return;
            }

            if (!this.isValidChecksum(data)) {
                log.error("Invalid packet checksum");
                ctx.writeAndFlush(new Packet(0xFA3));
                in.resetReaderIndex();
                return;
            }

            final int packetSize = BitKit.bytesToShort(data, 6);
            currentObjectLength = packetSize;
            if (packetSize + 8 < encryptedData.length) {
                final int receiveIndicator = this.receiveIndicator;
                data = decryptBytes(encryptedData, packetSize + 8);
                packet = new Packet(data);

                actualReaderIndex += packet.getPacketSize();

                if (logAllPackets)
                    log.debug("RECV [" + (ConfigService.getInstance().getValue("packets.id.translate.enabled", true) ? PacketOperations.getNameByValue(packet.getPacketId()) : String.format("0x%X", (int) packet.getPacketId())) + "] " + BitKit.toString(packet.getRawPacket(), 0, packet.getDataLength() + 8));

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

            actualReaderIndex += packet.getPacketSize();

            if (logAllPackets)
                log.debug("RECV [" + (ConfigService.getInstance().getValue("packets.id.translate.enabled", true) ? PacketOperations.getNameByValue(packet.getPacketId()) : String.format("0x%X", (int) packet.getPacketId())) + "] " + BitKit.toString(packet.getRawPacket(), 0, packet.getDataLength() + 8));

            out.add(packet);

            int newReceiveIndicator = receiveIndicator + 1;
            this.receiveIndicator = newReceiveIndicator % 60;
        }
        in.readerIndex(actualReaderIndex);
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
