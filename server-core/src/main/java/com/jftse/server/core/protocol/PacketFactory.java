package com.jftse.server.core.protocol;

@FunctionalInterface
public interface PacketFactory {
    IPacket create(byte[] data);
}
