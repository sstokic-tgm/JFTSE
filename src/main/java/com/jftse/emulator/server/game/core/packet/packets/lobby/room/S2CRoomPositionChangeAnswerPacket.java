package com.jftse.emulator.server.game.core.packet.packets.lobby.room;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CRoomPositionChangeAnswerPacket extends Packet {
    public S2CRoomPositionChangeAnswerPacket(char result, short oldPosition, short newPosition) {
        super(PacketID.S2CRoomPositionChangeAnswer);

        this.write(result, oldPosition, newPosition);
    }
}