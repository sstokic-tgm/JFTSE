package com.jftse.server.core.codec;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.emulator.common.utilities.LogConfigurator;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.SerialTable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.Logger;

public class PacketEncoder extends MessageToByteEncoder<Packet> {
    private static final int HEADER_SIZE = 8;
    private final byte[] encryptKey;
    private int sendIndicator = 0;
    private int header1Key = 0;

    private final boolean logAllPackets;
    private final boolean translatePacketIds;
    private final Logger log;

    public PacketEncoder(int encryptKey, Logger log) {
        this.encryptKey = BitKit.getBytes(encryptKey);

        this.logAllPackets = ConfigService.getInstance().getValue("logging.packets.all.enabled", true);
        final boolean logPacketsToConsole = ConfigService.getInstance().getValue("logging.packets.console-output.enabled", true);
        this.translatePacketIds = ConfigService.getInstance().getValue("packets.id.translate.enabled", true);
        LogConfigurator.setConsoleOutput("PacketLogger", logPacketsToConsole);

        this.log = log;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
        byte[] data = packet.getRawPacket();
        final int packetId = BitKit.bytesToChar(data, 4);
        final int length = data.length;

        out.ensureWritable(length);

        if (packetId != PacketOperations.S2CLoginWelcomePacket.getValue()) {
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

        if (logAllPackets)
            log.debug("SEND payload [{}] {}", translatePacketIds ? PacketOperations.getNameByValue(packetId) : String.format("0x%X", packetId), BitKit.toString(out, 0, out.writerIndex()));
    }

    private void createSerial(byte[] data) {
        final int sendIndicator = this.sendIndicator;

        int pos = (((this.header1Key << 4) - this.header1Key * 4 + sendIndicator) * 2);
        short header = BitKit.bytesToShort(SerialTable.serialTable, pos);

        data[0] = BitKit.getBytes(header)[0];
        data[1] = BitKit.getBytes(header)[1];

        int newSendIndicator = sendIndicator + 1;
        this.sendIndicator = newSendIndicator % 60;
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
