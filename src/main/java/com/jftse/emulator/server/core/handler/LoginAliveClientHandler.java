package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.networking.packet.Packet;

public class LoginAliveClientHandler extends AbstractHandler {
    @Override
    public boolean process(Packet packet) {
        return false;
    }

    @Override
    public void handle() {
        // empty
    }
}
