package com.jftse.emulator.server.core.handler.pet;

import com.jftse.emulator.server.core.packets.pet.S2CPetNameChangeAnswerPacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.shared.packets.CMSGDefault;
import com.jftse.server.core.shared.packets.pet.CMSGPetNameCheck;

//@PacketId(CMSGPetNameCheck.PACKET_ID)
public class PetNameChangeRequestPacketHandler implements PacketHandler<FTConnection, /* CMSGPetNameCheck */ CMSGDefault> {
    public PetNameChangeRequestPacketHandler() {}

    @Override
    public void handle(FTConnection connection, /* CMSGPetNameCheck */ CMSGDefault packet) {
        // To Do

        S2CPetNameChangeAnswerPacket petNameChangeAnswerPacket = new S2CPetNameChangeAnswerPacket((short) 0);
        connection.sendTCP(petNameChangeAnswerPacket);
    }
}
