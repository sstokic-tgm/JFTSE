package com.jftse.emulator.server.core.handler.pet;

import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.pet.CMSGPickupPet;
import com.jftse.server.core.shared.packets.pet.SMSGPickupPet;

//@PacketId(CMSGPickupPet.PACKET_ID)
public class PetPickupRequestPacketHandler implements PacketHandler<FTConnection, CMSGPickupPet> {
    public PetPickupRequestPacketHandler() {}

    @Override
    public void handle(FTConnection connection, CMSGPickupPet packet) {
        // To Do
        int newActivePetType = packet.getPetType();

        SMSGPickupPet response = SMSGPickupPet.builder().result((short) 0).petType(newActivePetType).build();
        connection.sendTCP(response);
    }
}
