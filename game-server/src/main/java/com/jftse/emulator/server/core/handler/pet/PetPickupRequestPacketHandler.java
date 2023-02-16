package com.jftse.emulator.server.core.handler.pet;

import com.jftse.emulator.server.core.packets.pet.C2SPetPickupRequestPacket;
import com.jftse.emulator.server.core.packets.pet.S2CPetPickupAnswerPacket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.protocol.Packet;

public class PetPickupRequestPacketHandler extends AbstractPacketHandler {
    private C2SPetPickupRequestPacket petPickupRequestPacket;

    public PetPickupRequestPacketHandler() {}

    @Override
    public boolean process(Packet packet) {
        petPickupRequestPacket = new C2SPetPickupRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        // To Do
        Integer newActivePetType = petPickupRequestPacket.getPetType();

        S2CPetPickupAnswerPacket petPickupAnswerPacket = new S2CPetPickupAnswerPacket((short) 0, newActivePetType);
        connection.sendTCP(petPickupAnswerPacket);
    }
}
