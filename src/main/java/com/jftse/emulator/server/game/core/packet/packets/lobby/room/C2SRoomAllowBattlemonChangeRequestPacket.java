package com.jftse.emulator.server.game.core.packet.packets.lobby.room;

import com.jftse.emulator.server.networking.packet.Packet;
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