package com.jftse.emulator.server.core.handler.game.pet;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.pet.S2CPetDataAnswerPacket;
import com.jftse.emulator.server.core.service.PetService;
import com.jftse.emulator.server.database.model.pet.Pet;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class PetDataRequestPacketHandler extends AbstractHandler {
    private final PetService petService;

    public PetDataRequestPacketHandler() {
        petService = ServiceManager.getInstance().getPetService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        List<Pet> petList = petService.findAllByPlayerId(connection.getClient().getActivePlayer().getId());

        S2CPetDataAnswerPacket petDataAnswerPacket = new S2CPetDataAnswerPacket(petList);
        connection.sendTCP(petDataAnswerPacket);
    }
}
