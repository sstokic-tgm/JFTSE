package com.ft.emulator.server.game.core.packet.packets.lobby.room;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CRoomPositionChangeAnswerPacket extends Packet {
    public S2CRoomPositionChangeAnswerPacket(char result, short oldPosition, short newPosition) {
        super(PacketID.S2CRoomPositionChangeAnswer);

        this.write(result, oldPosition, newPosition);
    }
}