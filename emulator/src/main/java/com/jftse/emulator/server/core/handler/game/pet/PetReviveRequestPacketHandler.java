package com.jftse.emulator.server.core.handler.game.pet;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.packet.packets.pet.C2SPetReviveRequestPacket;
import com.jftse.emulator.server.core.packet.packets.pet.S2CPetReviveAnswerPacket;
import com.jftse.emulator.server.networking.packet.Packet;

public class PetReviveRequestPacketHandler extends AbstractHandler {
    C2SPetReviveRequestPacket petReviveRequestPacket;

    public PetReviveRequestPacketHandler() {}

    @Override
    public boolean process(Packet packet) {
        petReviveRequestPacket = new C2SPetReviveRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        // To Do

        S2CPetReviveAnswerPacket petReviveAnswerPacket = new S2CPetReviveAnswerPacket((short) 0);
        connection.sendTCP(petReviveAnswerPacket);
    }
}
