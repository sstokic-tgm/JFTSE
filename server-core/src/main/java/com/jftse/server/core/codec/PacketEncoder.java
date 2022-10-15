package com.jftse.server.core.codec;

import com.jftse.emulator.common.utilities.BitKit;
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
        final int packetId = packet.getPacketId();
        byte[] data = packet.getRawPacket();
        byte[] encryptedData;
        this.createSerial(data);
        this.createCheckSum(data);

        if (packetId != PacketOperations.S2CLoginWelcomePacket.getValue()) {
            encryptedData = this.encryptBytes(data, data.length);
            out.writeBytes(encryptedData);

            log.debug("payload - SEND [" + PacketOperations.getNameByValue(packetId) + "] " + BitKit.toString(encryptedData, 0, encryptedData.length));
        } else {
            out.writeBytes(data);
            log.debug("payload - SEND [" + PacketOperations.getNameByValue(packetId) + "] " + BitKit.toString(data, 0, data.length));
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
