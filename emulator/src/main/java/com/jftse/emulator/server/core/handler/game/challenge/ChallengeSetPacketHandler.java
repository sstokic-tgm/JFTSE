package com.jftse.emulator.server.core.handler.game.challenge;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.networking.packet.Packet;

public class ChallengeSetPacketHandler extends AbstractHandler {
    @Override
    public boolean process(Packet packet) {
        return false;
    }

    @Override
    public void handle() {
        // empty
    }
}
