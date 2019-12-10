package com.ft.emulator.server.game.server.packets.room;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CRoomJoinAnswer extends Packet {

    public S2CRoomJoinAnswer(short result, byte roomType, byte unknown0, byte unknown1) {

        super(PacketID.S2CRoomJoinAnswer);

        this.write(result);
        this.write(roomType);
        this.write(unknown0);
        this.write(unknown1);
    }
}