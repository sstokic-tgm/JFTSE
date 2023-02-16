package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
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