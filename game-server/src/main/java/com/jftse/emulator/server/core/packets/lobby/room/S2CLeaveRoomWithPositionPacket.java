package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CLeaveRoomWithPositionPacket extends Packet {
    public S2CLeaveRoomWithPositionPacket(short position) {
        super(PacketOperations.S2CLeaveRoomWithPosition);

        this.write(position);
    }
}
