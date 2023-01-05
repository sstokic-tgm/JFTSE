package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomJoinAnswerPacket extends Packet {
    public S2CRoomJoinAnswerPacket(char result, byte unk0, byte unk1, byte unk2) {
        super(PacketOperations.S2CRoomJoinAnswer);

        this.write(result, unk0, unk1, unk2);
    }
}