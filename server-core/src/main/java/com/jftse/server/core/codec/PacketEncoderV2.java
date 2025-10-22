package com.jftse.server.core.codec;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.emulator.common.utilities.LogConfigurator;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.SerialTable;
import com.jftse.server.core.shared.packets.SMSGInitHandshake;
import com.jftse.server.core.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.Logger;

import java.util.stream.Stream;

public class PacketEncoderV2 extends MessageToByteEncoder<IPacket> {
    private static final int HEADER_SIZE = 8;

    private final byte[] encryptKey;
    private int sendIndicator = 0;
    private final int header1Key = 0;

    private final boolean logAllPackets;
    private final boolean prettyPrintEnabled;
    private final Logger log;

    public PacketEncoderV2(int encryptKey, Logger log) {
        this.encryptKey = BitKit.getBytes(encryptKey);

        this.logAllPackets = ConfigService.getInstance().getValue("logging.packets.all.enabled", true);
        final boolean logPacketsToConsole = ConfigService.getInstance().getValue("logging.packets.console-output.enabled", true);
        this.prettyPrintEnabled = ConfigService.getInstance().getValue("logging.packets.pretty-print.enabled", false);
        LogConfigurator.setConsoleOutput("PacketLogger", logPacketsToConsole);

        this.log = log;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, IPacket packet, ByteBuf out) throws Exception {
        byte[] data = packet.toBytes();
        final int packetId = BitKit.bytesToChar(data, 4);
        final int length = data.length;

        out.ensureWritable(length);

        if (packetId != SMSGInitHandshake.PACKET_ID) {
            int processed = 0;
            while (processed < length) {
                final int packetSize = BitKit.bytesToShort(data, 6);
                int currentObjectLength = packetSize + HEADER_SIZE;

                createSerial(data);
                createChecksum(data);

                for (int i = 0; i < currentObjectLength; i++) {
                    out.writeByte(data[i] ^ this.encryptKey[i & 3]);
                }

                processed += currentObjectLength;

                if (processed < length) {
                    BitKit.blockCopy(data, currentObjectLength, data, 0, length - processed);
                }
            }
        } else {
            out.writeBytes(data);
        }

        if (logAllPackets) {
            log.debug("SEND [{} bytes]\n{}\n--- Hex Dump ---\n{}\n===",
                    out.writerIndex(),
                    prettyPrintEnabled ? StringUtils.pretty(excludeStrings(packet)) : excludeStrings(packet),
                    BitKit.toString8x2(out, 0, out.writerIndex()));
        }
    }

    private String excludeStrings(IPacket packet) {
        String packetString = packet.toString();
        if (packetString.contains("password")) {
            return packetString.replaceAll("\"password\"\\s*:\\s*\"([^\"]+)\"", "\"password\":\"****\"");
        }
        if (packetString.contains("token")) {
            return packetString.replaceAll("\"token\"\\s*:\\s*\"([^\"]+)\"", "\"token\":\"****\"");
        }
        if (Stream.of("decKey", "encKey", "decTblIdx", "encTblIdx").anyMatch(packetString::contains)) {
            return packetString.replaceAll("\"(decKey|encKey|decTblIdx|encTblIdx)\"\\s*:\\s*(\\d+)", "\"$1\":****");
        }
        return packetString;
    }

    private void createSerial(byte[] data) {
        final int sendIndicator = this.sendIndicator;
        final int pos = sendIndicator * 2;
        short header = BitKit.bytesToShort(SerialTable.serialTable, pos);

        data[0] = BitKit.getBytes(header)[0];
        data[1] = BitKit.getBytes(header)[1];

        this.sendIndicator = (sendIndicator + 1) % 60;
    }

    private void createChecksum(byte[] data) {
        short checksum = (short) ((data[0] & 0xFF) + (data[1] & 0xFF) + (data[4] & 0xFF) + (data[5] & 0xFF) + (data[6] & 0xFF) + (data[7] & 0xFF));

        if (checksum % 2 == 0) {
            checksum += (short) 1587;
        } else {
            checksum += (short) 1568;
        }
        data[2] = BitKit.getBytes(checksum)[0];
        data[3] = BitKit.getBytes(checksum)[1];
    }
}
