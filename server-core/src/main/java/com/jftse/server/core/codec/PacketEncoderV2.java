package com.jftse.server.core.codec;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.emulator.common.utilities.LogConfigurator;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.SerialTable;
import com.jftse.server.core.shared.packets.SMSGInitHandshake;
import com.jftse.server.core.util.GsonUtils;
import com.jftse.server.core.util.ValidationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.Logger;

public class PacketEncoderV2 extends MessageToByteEncoder<IPacket> {
    private static final int HEADER_SIZE = 8;

    private final int encryptKey;
    private int sendIndicator = 0;
    private final int header1Key = 0;

    private final boolean logAllPackets;
    private final boolean prettyPrintEnabled;
    private final Logger log;

    public PacketEncoderV2(int encryptKey, Logger log) {
        this.encryptKey = encryptKey;

        this.logAllPackets = ConfigService.getInstance().getValue("logging.packets.all.enabled", true);
        final boolean logPacketsToConsole = ConfigService.getInstance().getValue("logging.packets.console-output.enabled", true);
        this.prettyPrintEnabled = ConfigService.getInstance().getValue("logging.packets.pretty-print.enabled", false);
        LogConfigurator.setConsoleOutput("PacketLogger", logPacketsToConsole);

        this.log = log;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, IPacket packet, ByteBuf out) throws Exception {
        byte[] data = packet.toBytes();
        final int length = data.length;
        if (length < HEADER_SIZE) {
            out.writeBytes(data);
            return;
        }

        final int packetId = BitKit.bytesToChar(data, 4);
        final boolean isHandshakePacket = packetId == SMSGInitHandshake.PACKET_ID;

        out.ensureWritable(length);

        if (!isHandshakePacket) {
            int offset = 0;
            while (offset < length) {
                final int packetSize = BitKit.bytesToShort(data, offset + 6);
                int currentPacketLength = packetSize + HEADER_SIZE;

                createSerial(data, offset);
                createChecksum(data, offset);

                for (int i = 0; i < currentPacketLength; i++) {
                    int kb = this.encryptKey >>> ((i & 3) * 8) & 0xFF;
                    out.writeByte(data[offset + i] ^ kb);
                }

                offset += currentPacketLength;
            }
        } else {
            out.writeBytes(data);
        }


        if (logAllPackets) {
            log.debug("SEND [{} bytes]\n{}\n--- Hex Dump ---\n{}\n===",
                    out.writerIndex(),
                    prettyPrintEnabled ? GsonUtils.pretty(ValidationUtil.sanitizeLogForEncode(packet.toString())) : ValidationUtil.sanitizeLogForEncode(packet.toString()),
                    BitKit.toString8x2(out, 0, out.writerIndex()));
        }
    }

    private void createSerial(byte[] data, int offset) {
        final int sendIndicator = this.sendIndicator;
        final int pos = sendIndicator * 2;

        data[offset] = SerialTable.serialTable[pos];
        data[offset + 1] = SerialTable.serialTable[pos + 1];

        this.sendIndicator = (sendIndicator + 1) % 60;
    }

    private void createChecksum(byte[] data, int offset) {
        int checksum = (data[offset] & 0xFF) +
                (data[offset + 1] & 0xFF) +
                (data[offset + 4] & 0xFF) +
                (data[offset + 5] & 0xFF) +
                (data[offset + 6] & 0xFF) +
                (data[offset + 7] & 0xFF);
        checksum += ((checksum & 1) == 0) ? 1587 : 1568;

        data[offset + 2] = (byte) (checksum & 0xFF);
        data[offset + 3] = (byte) ((checksum >>> 8) & 0xFF);
    }
}
