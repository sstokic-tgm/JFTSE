package com.jftse.emulator.server.core.handler;


import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractHandler {
    protected Connection connection;

    public abstract boolean process(Packet packet);
    public abstract void handle();
}
