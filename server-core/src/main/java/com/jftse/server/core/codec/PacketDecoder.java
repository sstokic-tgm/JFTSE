package com.jftse.server.core.codec;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.emulator.common.utilities.LogConfigurator;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.SerialTable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    private static final int HEADER_SIZE = 8;

    private final byte[] decryptKey;
    private int receiveIndicator = 0;
    private final int header1Key = 0;

    private final boolean logAllPackets;
    private final boolean translatePacketIds;
    private final Logger log;

    public PacketDecoder(int decryptKey, Logger log) {
        this.decryptKey = BitKit.getBytes(decryptKey);

        this.logAllPackets = ConfigService.getInstance().getValue("logging.packets.all.enabled", true);
        final boolean logPacketsToConsole = ConfigService.getInstance().getValue("logging.packets.console-output.enabled", true);
        this.translatePacketIds = ConfigService.getInstance().getValue("packets.id.translate.enabled", true);
        LogConfigurator.setConsoleOutput("PacketLogger", logPacketsToConsole);

        this.log = log;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Packet decoded = this.decode(in);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    protected Packet decode(ByteBuf in) throws Exception {
        final int length = in.readableBytes();
        if (length < HEADER_SIZE) {
            return null;
        }

        in.markReaderIndex();

        ByteBuf decryptedData = decryptBytes(in);

        if (logAllPackets)
            log.debug("RECV payload {}, readableBytes: {}", BitKit.toString(decryptedData, 0, decryptedData.readableBytes()), length);

        final int packetLength = decryptedData.getUnsignedShortLE(6);
        if (packetLength < 0 || packetLength + HEADER_SIZE > length) {
            in.resetReaderIndex();
            decryptedData.release();
            return null;
        }

        if (!this.isValidChecksum(decryptedData)) {
            log.error("Invalid packet checksum");
            in.resetReaderIndex();
            decryptedData.release();
            return null;
        }

        final int totalPacketSize = packetLength + HEADER_SIZE;
        if (length < totalPacketSize) {
            in.resetReaderIndex();
            decryptedData.release();
            return null;
        }

        byte[] data = new byte[totalPacketSize];
        decryptedData.getBytes(0, data);
        Packet packet = new Packet(data);

        decryptedData.release();
        in.readerIndex(in.readerIndex() + totalPacketSize);

        if (logAllPackets)
            log.debug("RECV [{}] {}", translatePacketIds ? PacketOperations.getNameByValue(packet.getPacketId()) : String.format("0x%X", (int) packet.getPacketId()), BitKit.toString(packet.getRawPacket(), 0, packet.getDataLength() + HEADER_SIZE));

        this.receiveIndicator = (this.receiveIndicator + 1) % 60;

        return packet;
    }

    private boolean isValidChecksum(ByteBuf in) {
        final int receiveIndicator = this.receiveIndicator;
        final int pos = (((this.header1Key << 4) - this.header1Key * 4 + receiveIndicator) * 2);
        final short serverSerial = BitKit.bytesToShort(SerialTable.serialTable, pos);

        byte[] serverSerialData = new byte[]{BitKit.getBytes(serverSerial)[0], BitKit.getBytes(serverSerial)[1]};
        try {
            final short clientChecksum = in.getShortLE(2);
            short serverChecksum = (short) ((serverSerialData[0] & 0xFF) + (serverSerialData[1] & 0xFF) + (in.getByte(4) & 0xFF) + (in.getByte(5) & 0xFF) + (in.getByte(6) & 0xFF) + (in.getByte(7) & 0xFF));
            if (serverChecksum % 2 == 0) {
                serverChecksum += (short) 1587;
            } else {
                serverChecksum += (short) 1568;
            }

            return clientChecksum == serverChecksum;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private ByteBuf decryptBytes(ByteBuf in) {
        ByteBuf decryptedData = in.copy();
        for (int i = 0; i < decryptedData.readableBytes(); i++) {
            decryptedData.setByte(i, decryptedData.getByte(i) ^ this.decryptKey[(i & 3)]);
        }
        return decryptedData;
    }
}
