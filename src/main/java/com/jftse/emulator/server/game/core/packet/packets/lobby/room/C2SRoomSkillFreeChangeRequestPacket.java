package com.jftse.emulator.server.game.core.packet.packets.lobby.room;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomSkillFreeChangeRequestPacket extends Packet {
    private boolean skillFree;

    public C2SRoomSkillFreeChangeRequestPacket(Packet packet) {
        super(packet);

        this.skillFree = this.readByte() == 1;
    }
}