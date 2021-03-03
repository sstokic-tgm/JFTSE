package com.jftse.emulator.server.game.core.packet.packets.lobby.room;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CRoomCreateAnswerPacket extends Packet {
    public S2CRoomCreateAnswerPacket(char result, byte unk0, byte unk1, byte unk2) {
        super(PacketID.S2CRoomCreateAnswer);

        this.write(result, unk0, unk1, unk2);
    }
}