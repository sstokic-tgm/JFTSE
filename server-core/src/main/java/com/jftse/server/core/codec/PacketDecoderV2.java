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
            log.error("RECV [{} bytes]\nInvalid packet checksum\n--- Hex Dump ---\n{}\n===",
                    decryptedData.readableBytes(),
                    BitKit.toString8x2(decryptedData, 0, decryptedData.readableBytes()));

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
        final int packetChecksum = in.getUnsignedShortLE(2);

        try {
            final int c0 = in.getUnsignedByte(0);
            final int c1 = in.getUnsignedByte(1);
            final int c4 = in.getUnsignedByte(4);
            final int c5 = in.getUnsignedByte(5);
            final int c6 = in.getUnsignedByte(6);
            final int c7 = in.getUnsignedByte(7);

            final int s0 = SerialTable.serialTable[pos] & 0xFF;
            final int s1 = SerialTable.serialTable[pos + 1] & 0xFF;

            int recomputed = c0 + c1 + c4 + c5 + c6 + c7;
            recomputed += (recomputed % 2 == 0) ? 1587 : 1568;
            recomputed &= 0xFFFF;

            if (packetChecksum != recomputed) {
                return false;
            }

            return s0 == c0 && s1 == c1;
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
