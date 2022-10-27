package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomMapChangeAnswerPacket extends Packet {
    public S2CRoomMapChangeAnswerPacket(byte map) {
        super(PacketOperations.S2CRoomMapChangeAnswer.getValue());

        this.write(map);
    }
}