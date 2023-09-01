package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomPositionSwapPacket extends Packet {
    public S2CRoomPositionSwapPacket(short oldPosition, short newPosition) {
        super(PacketOperations.S2CRoomPositionSwap);

        this.write(oldPosition, newPosition);
    }
}
