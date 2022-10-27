package com.jftse.emulator.server.core.handler.pet;

import com.jftse.emulator.server.core.packets.pet.C2SPetNameChangeRequestPacket;
import com.jftse.emulator.server.core.packets.pet.S2CPetNameChangeAnswerPacket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.protocol.Packet;

public class PetNameChangeRequestPacketHandler extends AbstractPacketHandler {
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
