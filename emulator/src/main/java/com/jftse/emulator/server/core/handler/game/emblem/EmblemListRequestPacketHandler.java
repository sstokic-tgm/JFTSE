package com.jftse.emulator.server.core.handler.game.emblem;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.networking.packet.Packet;

public class EmblemListRequestPacketHandler extends AbstractHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        // empty..
    }
}
