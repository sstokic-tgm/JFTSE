package com.jftse.server.core.codec;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.emulator.common.utilities.LogConfigurator;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.PacketRegistry;
import com.jftse.server.core.protocol.SerialTable;
import com.jftse.server.core.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class PacketDecoderV2 extends ByteToMessageDecoder {
    private static final int HEADER_SIZE = 8;

    private final byte[] decryptKey;
    private int receiveIndicator = 0;
    private final int header1Key = 0;

    private final boolean logAllPackets;
    private final boolean prettyPrintEnabled;
    private final Logger log;

    public PacketDecoderV2(int decryptKey, Logger log) {
        this.decryptKey = BitKit.getBytes(decryptKey);

        this.logAllPackets = ConfigService.getInstance().getValue("logging.packets.all.enabled", true);
        final boolean logPacketsToConsole = ConfigService.getInstance().getValue("logging.packets.console-output.enabled", true);
        this.prettyPrintEnabled = ConfigService.getInstance().getValue("logging.packets.pretty-print.enabled", false);
        LogConfigurator.setConsoleOutput("PacketLogger", logPacketsToConsole);

        this.log = log;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        IPacket decoded = this.decode(in);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    protected IPacket decode(ByteBuf in) throws Exception {
        final int length = in.readableBytes();
        if (length < HEADER_SIZE) {
            return null;
        }
        in.markReaderIndex();

        ByteBuf decryptedData = decryptBytes(in);

        final int packetId = decryptedData.getUnsignedShortLE(4);
        final int packetLength = decryptedData.getUnsignedShortLE(6);
        if (packetLength < 0 || packetLength + HEADER_SIZE > length) {
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

        if (!this.isValidChecksum(decryptedData)) {
            log.error("Invalid packet checksum for packet: {}", BitKit.toString(decryptedData, 0, decryptedData.readableBytes()));
            in.resetReaderIndex();
            decryptedData.release();
            return null;
        }

        final byte[] data = new byte[totalPacketSize];
        decryptedData.getBytes(0, data);
        decryptedData.release();
        in.skipBytes(totalPacketSize);

        IPacket packet = PacketRegistry.decode(packetId, data);

        if (logAllPackets) {
            log.debug("RECV [{} bytes]\n{}\n--- Hex Dump ---\n{}\n===",
                    data.length,
                    prettyPrintEnabled ? StringUtils.pretty(excludeStrings(packet)) : excludeStrings(packet),
                    BitKit.toString8x2(data, 0, data.length));
        }

        this.receiveIndicator = (this.receiveIndicator + 1) % 60;
        return packet;
    }

    private String excludeStrings(IPacket packet) {
        String packetString = packet.toString();
        if (packetString.contains("password")) {
            return packetString.replaceAll("\"password\"\\s*:\\s*\"([^\"]+)\"", "\"password\":\"****\"");
        }
        if (packetString.contains("token")) {
            return packetString.replaceAll("\"token\"\\s*:\\s*\"([^\"]+)\"", "\"token\":\"****\"");
        }
        return packetString;
    }

    private boolean isValidChecksum(ByteBuf in) {
        final int receiveIndicator = this.receiveIndicator;
        final int pos = receiveIndicator * 2;
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
