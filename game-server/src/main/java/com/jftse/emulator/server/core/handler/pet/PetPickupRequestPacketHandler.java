package com.jftse.emulator.server.core.handler.pet;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.pet.Pet;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.PetService;
import com.jftse.server.core.shared.packets.pet.CMSGPickupPet;
import com.jftse.server.core.shared.packets.pet.SMSGPickupPet;

import java.util.List;

@PacketId(CMSGPickupPet.PACKET_ID)
public class PetPickupRequestPacketHandler implements PacketHandler<FTConnection, CMSGPickupPet> {
    private final PetService petService;

    public PetPickupRequestPacketHandler() {
        petService = ServiceManager.getInstance().getPetService();
    }

    @Override
    public void handle(FTConnection connection, CMSGPickupPet packet) {
        FTClient ftClient = connection.getClient();
        if (!ftClient.hasPlayer()) {
            return;
        }

        int newActivePetType = packet.getPetType();
        List<Pet> petList = petService.findAllByPlayerId(ftClient.getPlayer().getId());
        petList.removeIf(pet -> pet.getType() != (byte) newActivePetType);

        SMSGPickupPet petPickup;
        if (newActivePetType == -1) {
            ftClient.setActivePet(null);
            petPickup = SMSGPickupPet.builder()
                    .result((short) 0)
                    .petType(newActivePetType)
                    .build();
        } else {
            ftClient.setActivePet(petList.getFirst());
            petPickup = SMSGPickupPet.builder()
                    .result((short) 0)
                    .petType(petList.getFirst().getType().intValue())
                    .build();
        }
        connection.sendTCP(petPickup);
    }
}
