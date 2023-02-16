package com.jftse.emulator.server.core.handler.game.pet;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.packet.packets.pet.C2SPetPickupRequestPacket;
import com.jftse.emulator.server.core.packet.packets.pet.S2CPetPickupAnswerPacket;
import com.jftse.emulator.server.networking.packet.Packet;

public class PetPickupRequestPacketHandler extends AbstractHandler {
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
