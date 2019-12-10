package com.ft.emulator.server.game.server.packets.room;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CRoomPositionChangeAnswer extends Packet {

    public S2CRoomPositionChangeAnswer(char result, char oldPosition, char newPosition) {

        super(PacketID.S2CRoomPositionChangeAnswer);

        this.write(result);
        this.write(oldPosition);
        this.write(newPosition);
    }
}