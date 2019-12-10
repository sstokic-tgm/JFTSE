package com.ft.emulator.server.game.server.packets.room;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CRoomMapChangeAnswerPacket extends Packet {

    public S2CRoomMapChangeAnswerPacket(byte map) {

        super(PacketID.S2CRoomMapChangeAnswer);

        this.write(map);
    }
}