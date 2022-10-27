package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomAllowBattlemonChangeRequestPacket extends Packet {
    private byte allowBattlemon;

    public C2SRoomAllowBattlemonChangeRequestPacket(Packet packet) {
        super(packet);

        this.allowBattlemon = this.readByte();
    }
}