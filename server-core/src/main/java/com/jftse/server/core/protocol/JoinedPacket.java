package com.jftse.server.core.protocol;

import com.jftse.emulator.common.utilities.BitKit;
import lombok.Getter;

@Getter
public class JoinedPacket extends Packet {
    private final byte[] joinedData;

    public JoinedPacket(Packet... packets) {
        int length = 0;
        for (Packet packet : packets) {
            length += packet.getDataLength() + 8;
        }

        joinedData = new byte[length];

        int offset = 0;
        for (Packet packet : packets) {
            final int packetLength = packet.getDataLength() + 8;
            BitKit.blockCopy(packet.getRawPacket(), 0, joinedData, offset, packetLength);
            offset += packetLength;
        }
    }

    @Override
    public byte[] getRawPacket() {
        return joinedData;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "data=" + BitKit.toString(joinedData, 0, joinedData.length) +
                '}';
    }
}
