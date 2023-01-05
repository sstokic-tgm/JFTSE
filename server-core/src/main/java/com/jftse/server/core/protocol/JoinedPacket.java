package com.jftse.server.core.protocol;

import com.jftse.emulator.common.utilities.BitKit;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Getter
public class JoinedPacket extends Packet {
    private final ByteBuffer joinedData;

    public JoinedPacket(Packet... packets) {
        int length = 0;
        for (Packet packet : packets) {
            length += packet.getDataLength() + 8;
        }

        joinedData = ByteBuffer.allocate(length);
        joinedData.order(ByteOrder.nativeOrder());

        for (Packet packet : packets) {
            joinedData.put(packet.getRawPacket());
        }
    }

    @Override
    public byte[] getRawPacket() {
        joinedData.flip();
        return joinedData.array();
    }

    @Override
    public String toString() {
        joinedData.flip();
        return "Packet{" +
                "data=" + BitKit.toString(joinedData.array(), 0, joinedData.capacity()) +
                '}';
    }
}
