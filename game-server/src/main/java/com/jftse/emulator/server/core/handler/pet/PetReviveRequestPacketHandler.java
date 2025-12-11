package com.jftse.emulator.server.core.handler.pet;

import com.jftse.emulator.server.core.packets.pet.S2CPetReviveAnswerPacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.shared.packets.CMSGDefault;

//@PacketId(CMSGPetNameCheck.PACKET_ID)
public class PetReviveRequestPacketHandler implements PacketHandler<FTConnection, /* CMSGRevivePet */ CMSGDefault> {
    public PetReviveRequestPacketHandler() {}

    @Override
    public void handle(FTConnection connection, /* CMSGRevivePet */ CMSGDefault packet) {
        // To Do

        S2CPetReviveAnswerPacket petReviveAnswerPacket = new S2CPetReviveAnswerPacket((short) 0);
        connection.sendTCP(petReviveAnswerPacket);
    }
}
