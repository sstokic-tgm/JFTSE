package com.jftse.emulator.server.core.handler.game.pet;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.packet.packets.pet.C2SPetNameChangeRequestPacket;
import com.jftse.emulator.server.core.packet.packets.pet.S2CPetNameChangeAnswerPacket;
import com.jftse.emulator.server.networking.packet.Packet;

public class PetNameChangeRequestPacketHandler extends AbstractHandler {
    private C2SPetNameChangeRequestPacket petNameChangeRequestPacket;

    public PetNameChangeRequestPacketHandler() {}

    @Override
    public boolean process(Packet packet) {
        petNameChangeRequestPacket = new C2SPetNameChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        // To Do

        S2CPetNameChangeAnswerPacket petNameChangeAnswerPacket = new S2CPetNameChangeAnswerPacket((short) 0);
        connection.sendTCP(petNameChangeAnswerPacket);
    }
}
