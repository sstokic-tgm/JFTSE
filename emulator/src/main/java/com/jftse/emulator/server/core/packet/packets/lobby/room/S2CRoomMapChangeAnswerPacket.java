package com.jftse.emulator.server.core.packet.packets.lobby.room;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CRoomMapChangeAnswerPacket extends Packet {
    public S2CRoomMapChangeAnswerPacket(byte map) {
        super(PacketOperations.S2CRoomMapChangeAnswer.getValueAsChar());

        this.write(map);
    }
}