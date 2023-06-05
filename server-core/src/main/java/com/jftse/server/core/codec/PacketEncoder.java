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
    private final byte[] encryptKey;
    private int sendIndicator = 0;
    private int header1Key = 0;

    private final Logger log;

    public PacketEncoder(int encryptKey, Logger log) {
        this.encryptKey = BitKit.getBytes(encryptKey);
        this.log = log;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
        boolean logAllPackets = ConfigService.getInstance().getValue("logging.packets.all.enabled", true);

        boolean logPacketsToConsole = ConfigService.getInstance().getValue("logging.packets.console-output.enabled", true);
        LogConfigurator.setConsoleOutput("PacketLogger", logPacketsToConsole);

        byte[] data = packet.getRawPacket();
        final int packetId = BitKit.bytesToChar(data, 4);
        byte[] encryptedResult = new byte[data.length];

        if (packetId != PacketOperations.S2CLoginWelcomePacket.getValue()) {
            int destPos = 0;
            int currentObjectLength = 0;
            while (true) {
                final int packetSize = BitKit.bytesToShort(data, 6);
                currentObjectLength = packetSize;
                if (packetSize + 8 < data.length) {
                    createSerial(data);
                    createCheckSum(data);
                    byte[] encryptedTmpPacket = encryptBytes(data, packetSize + 8);
                    BitKit.blockCopy(encryptedTmpPacket, 0, encryptedResult, destPos, encryptedTmpPacket.length);

                    destPos += packetSize + 8;

                    byte[] tmp = new byte[data.length - 8 - packetSize];
                    BitKit.blockCopy(data, packetSize + 8, tmp, 0, tmp.length);
                    data = new byte[tmp.length];
                    BitKit.blockCopy(tmp, 0, data, 0, data.length);
                } else {
                    break;
                }
            }
            if (currentObjectLength + 8 <= data.length) {
                createSerial(data);
                createCheckSum(data);
                byte[] encryptedPacket = encryptBytes(data, data.length);
                BitKit.blockCopy(encryptedPacket, 0, encryptedResult, destPos, encryptedPacket.length);
            }
            out.writeBytes(encryptedResult);
            if (logAllPackets)
                log.debug("SEND payload [" + (ConfigService.getInstance().getValue("packets.id.translate.enabled", true) ? PacketOperations.getNameByValue(packetId) : String.format("0x%X", packetId)) + "] " + BitKit.toString(encryptedResult, 0, encryptedResult.length));
        } else {
            out.writeBytes(data);
            if (logAllPackets)
                log.debug("SEND payload [" + (ConfigService.getInstance().getValue("packets.id.translate.enabled", true) ? PacketOperations.getNameByValue(packetId) : String.format("0x%X", packetId)) + "] " + BitKit.toString(data, 0, data.length));
        }
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

    private byte[] encryptBytes(byte[] decryptedBuffer, int size) {
        byte[] encrypted = new byte[size];
        BitKit.blockCopy(decryptedBuffer, 0, encrypted, 0, size);

        for (int i = 0; i < size; i++)
            encrypted[i] ^= this.encryptKey[(i & 3)];

        return encrypted;
    }
}
