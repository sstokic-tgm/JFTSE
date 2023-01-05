package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomPositionChangeAnswerPacket extends Packet {
    public S2CRoomPositionChangeAnswerPacket(char result, short oldPosition, short newPosition) {
        super(PacketOperations.S2CRoomPositionChangeAnswer);

        this.write(result, oldPosition, newPosition);
    }
}