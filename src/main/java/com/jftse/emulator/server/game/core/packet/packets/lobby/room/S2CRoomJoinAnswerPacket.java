package com.jftse.emulator.server.game.core.packet.packets.lobby.room;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CRoomJoinAnswerPacket extends Packet {
    public S2CRoomJoinAnswerPacket(char result, byte unk0, byte unk1, byte unk2) {
        super(PacketID.S2CRoomJoinAnswer);

        this.write(result, unk0, unk1, unk2);
    }
}