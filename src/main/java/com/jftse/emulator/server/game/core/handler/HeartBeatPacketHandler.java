package com.jftse.emulator.server.game.core.handler;

import com.jftse.emulator.server.networking.packet.Packet;

public class HeartBeatPacketHandler extends AbstractHandler {
    @Override
    public boolean process(Packet packet) {
        return false;
    }

    @Override
    public void handle() {
        // empty
    }
}
