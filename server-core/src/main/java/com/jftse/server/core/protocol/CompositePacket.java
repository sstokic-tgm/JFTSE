package com.jftse.server.core.protocol;

import com.jftse.emulator.common.utilities.BitKit;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CompositePacket extends Packet {
    private final List<IPacket> packets;

    public CompositePacket(IPacket... packets) {
        this.packets = List.of(packets);
    }

    @Override
    public byte[] toBytes() {
        List<byte[]> encodedPackets = new ArrayList<>(packets.size());
        int totalLength = 0;
        for (IPacket packet : packets) {
            byte[] encoded = packet.toBytes();
            PacketFrames.validate(encoded);
            totalLength = Math.addExact(totalLength, encoded.length);
            encodedPackets.add(encoded);
        }
        byte[] data = new byte[totalLength];
        int offset = 0;
        for (byte[] encoded : encodedPackets) {
            BitKit.blockCopy(encoded, 0, data, offset, encoded.length);
            offset += encoded.length;
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
