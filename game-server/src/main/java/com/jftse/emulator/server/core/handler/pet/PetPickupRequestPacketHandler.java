package com.jftse.emulator.server.core.handler.pet;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.pet.C2SPetPickupRequestPacket;
import com.jftse.emulator.server.core.packets.pet.S2CPetPickupAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.pet.Pet;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PetService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SPetPickupRequest)
public class PetPickupRequestPacketHandler extends AbstractPacketHandler {
    private C2SPetPickupRequestPacket petPickupRequestPacket;

    private final PetService petService;

    public PetPickupRequestPacketHandler() {
        petService = ServiceManager.getInstance().getPetService();
    }

    @Override
    public boolean process(Packet packet) {
        petPickupRequestPacket = new C2SPetPickupRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();

        Integer newActivePetType = petPickupRequestPacket.getPetType();
        List<Pet> petList = petService.findAllByPlayerId(ftClient.getPlayer().getId());
        petList.removeIf(pet -> pet.getType() != newActivePetType.byteValue());

        S2CPetPickupAnswerPacket petPickupAnswerPacket;
        if (newActivePetType == -1) {
            ftClient.setActivePet(null);
            petPickupAnswerPacket = new S2CPetPickupAnswerPacket((short) 0, newActivePetType);
        } else {
            ftClient.setActivePet(petList.getFirst());
            petPickupAnswerPacket = new S2CPetPickupAnswerPacket((short) 0, petList.getFirst().getType().intValue());
        }
        connection.sendTCP(petPickupAnswerPacket);
    }
}
