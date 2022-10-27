package com.jftse.server.core.protocol;

import com.jftse.emulator.common.utilities.BitKit;
import lombok.Getter;

@Getter
public class JoinedPacket extends Packet {
    byte[] joinedData;

    public JoinedPacket(Packet... packets) {
        super(true);

        int length = 0;
        for (Packet packet : packets) {
            length += packet.getDataLength() + 8;
        }
        joinedData = new byte[length];
        int destPos = 0;
        for (Packet packet : packets) {
            BitKit.blockCopy(packet.getRawPacket(), 0, joinedData, destPos, packet.getDataLength() + 8);
            destPos += packet.getDataLength() + 8;
        }
    }

    @Override
    public String toString() {
        return "Packet{" +
                "data=" + BitKit.toString(joinedData, 0, joinedData.length) +
                '}';
    }
}
