package com.jftse.emulator.server.core.handler.pet;

import com.jftse.emulator.server.core.packets.pet.C2SPetReviveRequestPacket;
import com.jftse.emulator.server.core.packets.pet.S2CPetReviveAnswerPacket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.protocol.Packet;

public class PetReviveRequestPacketHandler extends AbstractPacketHandler {
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
