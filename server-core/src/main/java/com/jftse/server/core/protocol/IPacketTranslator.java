package com.jftse.server.core.protocol;

public interface IPacketTranslator<T extends IPacket, U extends IPacket> {
    T translate(U packet);
}
