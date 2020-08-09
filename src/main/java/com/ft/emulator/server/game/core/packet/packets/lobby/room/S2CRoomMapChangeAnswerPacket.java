package com.ft.emulator.server.game.core.packet.packets.lobby.room;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CRoomMapChangeAnswerPacket extends Packet {
    public S2CRoomMapChangeAnswerPacket(byte map) {
        super(PacketID.S2CRoomMapChangeAnswer);

        this.write(map);
    }
}