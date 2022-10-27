package com.jftse.server.core.handler;

import com.jftse.server.core.net.Connection;
import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractPacketHandler {
    protected Connection<?> connection;

    public abstract boolean process(Packet packet);
    public abstract void handle();
}
