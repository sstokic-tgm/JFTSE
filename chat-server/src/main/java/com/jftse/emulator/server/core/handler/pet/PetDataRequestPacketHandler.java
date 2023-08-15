package com.jftse.emulator.server.core.handler.pet;

import com.jftse.emulator.server.core.packets.pet.S2CPetDataAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.pet.Pet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PetService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SPetDataRequest)
public class PetDataRequestPacketHandler extends AbstractPacketHandler {
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
        FTClient ftClient = (FTClient) connection.getClient();
        List<Pet> petList = petService.findAllByPlayerId(ftClient.getPlayer().getId());

        S2CPetDataAnswerPacket petDataAnswerPacket = new S2CPetDataAnswerPacket(petList);
        connection.sendTCP(petDataAnswerPacket);
    }
}
