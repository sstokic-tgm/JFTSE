package com.jftse.emulator.server.core.handler.pet;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.pet.S2CPetDataAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.pet.Pet;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.PetService;
import com.jftse.server.core.shared.packets.pet.CMSGPetData;

import java.util.List;

@PacketId(CMSGPetData.PACKET_ID)
public class PetDataRequestPacketHandler implements PacketHandler<FTConnection, CMSGPetData> {
    private final PetService petService;

    public PetDataRequestPacketHandler() {
        petService = ServiceManager.getInstance().getPetService();
    }

    @Override
    public void handle(FTConnection connection, CMSGPetData packet) {
        FTClient ftClient = connection.getClient();
        List<Pet> petList = petService.findAllByPlayerId(ftClient.getPlayer().getId());

        S2CPetDataAnswerPacket petDataAnswerPacket = new S2CPetDataAnswerPacket(petList);
        connection.sendTCP(petDataAnswerPacket);
    }
}
