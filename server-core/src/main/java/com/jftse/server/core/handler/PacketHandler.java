package com.jftse.server.core.handler;

import com.jftse.server.core.net.Client;
import com.jftse.server.core.net.Connection;
import com.jftse.server.core.protocol.IPacket;

@FunctionalInterface
public interface PacketHandler<C extends Connection<? extends Client<C>>, T extends IPacket> {
    void handle(C connection, T packet) throws Exception;
}
