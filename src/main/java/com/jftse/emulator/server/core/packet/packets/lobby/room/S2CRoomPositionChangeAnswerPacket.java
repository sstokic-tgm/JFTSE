package com.jftse.emulator.server.core.packet.packets.lobby.room;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CRoomPositionChangeAnswerPacket extends Packet {
    public S2CRoomPositionChangeAnswerPacket(char result, short oldPosition, short newPosition) {
        super(PacketOperations.S2CRoomPositionChangeAnswer.getValueAsChar());

        this.write(result, oldPosition, newPosition);
    }
}