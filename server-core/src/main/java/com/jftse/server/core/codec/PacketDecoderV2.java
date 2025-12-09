package com.jftse.server.core.codec;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.emulator.common.utilities.LogConfigurator;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.PacketRegistry;
import com.jftse.server.core.protocol.SerialTable;
import com.jftse.server.core.util.GsonUtils;
import com.jftse.server.core.util.ValidationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class PacketDecoderV2 extends ByteToMessageDecoder {
    private static final int HEADER_SIZE = 8;

    private final byte[] header = new byte[HEADER_SIZE];

    private final int decryptKey;
    private int receiveIndicator = 0;
    private final int header1Key = 0;

    private final boolean logAllPackets;
    private final boolean prettyPrintEnabled;
    private final Logger log;

    public PacketDecoderV2(int decryptKey, Logger log) {
        this.decryptKey = decryptKey;

        this.logAllPackets = ConfigService.getInstance().getValue("logging.packets.all.enabled", true);
        final boolean logPacketsToConsole = ConfigService.getInstance().getValue("logging.packets.console-output.enabled", true);
        this.prettyPrintEnabled = ConfigService.getInstance().getValue("logging.packets.pretty-print.enabled", false);
        LogConfigurator.setConsoleOutput("PacketLogger", logPacketsToConsole);

        this.log = log;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        IPacket decoded = decode(in);
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

        decryptHeader(in, in.readerIndex(), header);
        final int packetId = BitKit.bytesToShort(header, 4);
        final int packetLength = BitKit.bytesToShort(header, 6);
        final int totalPacketSize = packetLength + HEADER_SIZE;

        if (packetLength < 0 || length < totalPacketSize) {
            in.resetReaderIndex();
            return null;
        }

        final byte[] data = new byte[totalPacketSize];
        decryptBytes(in, in.readerIndex(), data, totalPacketSize);

        if (!this.isValidChecksum(data)) {
            log.error("RECV [{} bytes]\nInvalid packet checksum\n--- Hex Dump ---\n{}\n===",
                    totalPacketSize,
                    BitKit.toString8x2(data, 0, totalPacketSize));

            in.skipBytes(totalPacketSize); // consume invalid to avoid decode loop
            return null;
        }

        in.skipBytes(totalPacketSize);
        IPacket packet = PacketRegistry.decode(packetId, data);

        if (logAllPackets) {
            log.debug("RECV [{} bytes]\n{}\n--- Hex Dump ---\n{}\n===",
                    data.length,
                    prettyPrintEnabled ? GsonUtils.pretty(ValidationUtil.sanitizeLogForDecode(packet.toString())) : ValidationUtil.sanitizeLogForDecode(packet.toString()),
                    BitKit.toString8x2(data, 0, data.length));
        }

        this.receiveIndicator = (this.receiveIndicator + 1) % 60;
        return packet;
    }

    private boolean isValidChecksum(byte[] data) {
        final int receiveIndicator = this.receiveIndicator;
        final int pos = receiveIndicator * 2;
        final int packetChecksum = BitKit.bytesToShort(data, 2);

        final int c0 = data[0] & 0xFF;
        final int c1 = data[1] & 0xFF;
        final int c4 = data[4] & 0xFF;
        final int c5 = data[5] & 0xFF;
        final int c6 = data[6] & 0xFF;
        final int c7 = data[7] & 0xFF;

        final int s0 = SerialTable.serialTable[pos] & 0xFF;
        final int s1 = SerialTable.serialTable[pos + 1] & 0xFF;

        int recomputed = c0 + c1 + c4 + c5 + c6 + c7;
        recomputed += ((recomputed & 1) == 0) ? 1587 : 1568;
        recomputed &= 0xFFFF;

        if (packetChecksum != recomputed) {
            return false;
        }

        return s0 == c0 && s1 == c1;
    }

    private void decryptBytes(ByteBuf in, int offset, byte[] out, int length) {
        int i = 0;
        // 4-byte chunks
        for (; i + 4 <= length; i += 4) {
            int k = in.getIntLE(offset + i) ^ this.decryptKey;
            out[i] = (byte) k;
            out[i + 1] = (byte) (k >>> 8);
            out[i + 2] = (byte) (k >>> 16);
            out[i + 3] = (byte) (k >>> 24);
        }

        // tail
        for (; i < length; i++) {
            int b = in.getUnsignedByte(offset + i);
            int kb = this.decryptKey >>> ((i & 3) * 8) & 0xFF;
            out[i] = (byte) (b ^ kb);
        }
    }

    private void decryptHeader(ByteBuf in, int offset, byte[] out) {
        for (int i = 0; i < HEADER_SIZE; i++) {
            int b = in.getUnsignedByte(offset + i);
            int kb = this.decryptKey >>> ((i & 3) * 8) & 0xFF;
            out[i] = (byte) (b ^ kb);
        }
    }
}
