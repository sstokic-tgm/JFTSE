package com.jftse.server.core.protocol;

import com.jftse.emulator.common.utilities.BitKit;

import java.util.List;
import java.util.stream.Collectors;

public class CompositePacket implements IPacket {
    private final List<IPacket> packets;

    public CompositePacket(IPacket... packets) {
        this.packets = List.of(packets);
    }

    @Override
    public byte[] toBytes() {
        final int totalLength = packets.stream().mapToInt(p -> p.getDataLength() + 8).sum();
        byte[] data = new byte[totalLength];
        int offset = 0;
        for (IPacket packet : packets) {
            final int packetLength = packet.getDataLength() + 8;
            BitKit.blockCopy(packet.toBytes(), 0, data, offset, packetLength);
            offset += packetLength;
        }
        return data;
    }

    @Override
    public char getDataLength() {
        return (char) packets.stream().mapToInt(p -> p.getDataLength() + 8).sum();
    }

    @Override
    public char getPacketId() {
        return 0;
    }

    @Override
    public char getCheckSerial() {
        return 0;
    }

    @Override
    public char getCheckSum() {
        return 0;
    }

    @Override
    public String toString() {
        return packets.stream().map(IPacket::toString).collect(Collectors.joining(", "));
    }
}
