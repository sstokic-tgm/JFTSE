package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomReadyChangeAnswerPacket extends Packet {
    public S2CRoomReadyChangeAnswerPacket(short position, boolean ready) {
        super(PacketOperations.S2CRoomReadyChange);

        this.write(position);
        this.write(ready);
    }
}
