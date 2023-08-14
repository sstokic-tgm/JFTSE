package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomJoinAnswerPacket extends Packet {
    public S2CRoomJoinAnswerPacket(char result, byte roomType, int mode, byte mapId) {
        super(PacketOperations.S2CRoomJoinAnswer);

        this.write(result, roomType, mode, mapId);
    }
}