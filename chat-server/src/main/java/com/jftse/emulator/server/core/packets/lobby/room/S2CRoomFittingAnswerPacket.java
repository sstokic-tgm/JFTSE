package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomFittingAnswerPacket extends Packet {
    public S2CRoomFittingAnswerPacket(short position, boolean ready) {
        super(PacketOperations.S2CRoomFittingAnswer);

        this.write(position);
        this.write(ready);
    }
}
