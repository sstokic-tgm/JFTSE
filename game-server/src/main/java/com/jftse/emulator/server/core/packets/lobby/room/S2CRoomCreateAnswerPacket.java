package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomCreateAnswerPacket extends Packet {
    public S2CRoomCreateAnswerPacket(char result, byte unk0, byte unk1, byte unk2) {
        super(PacketOperations.S2CRoomCreateAnswer);

        this.write(result, unk0, unk1, unk2);
    }
}