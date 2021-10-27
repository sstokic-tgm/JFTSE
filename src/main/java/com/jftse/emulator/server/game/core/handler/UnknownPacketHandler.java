package com.jftse.emulator.server.game.core.handler;

import com.jftse.emulator.server.networking.packet.Packet;

public class UnknownPacketHandler extends AbstractHandler {
    private Packet packet;

    @Override
    public boolean process(Packet packet) {
        this.packet = packet;
        return true;
    }

    @Override
    public void handle() {
        Packet unknownAnswer = new Packet((char) (packet.getPacketId() + 1));
        unknownAnswer.write((short) 0);
        connection.sendTCP(unknownAnswer);
    }
}
