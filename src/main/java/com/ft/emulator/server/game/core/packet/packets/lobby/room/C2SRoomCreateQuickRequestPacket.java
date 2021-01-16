package com.ft.emulator.server.game.core.packet.packets.lobby.room;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomCreateQuickRequestPacket extends Packet {
    private byte allowBattlemon;
    private byte mode;
    private byte players;

    public C2SRoomCreateQuickRequestPacket(Packet packet) {
        super(packet);

        this.allowBattlemon = this.readByte();
        this.mode = this.readByte();
        this.players = this.readByte();
    }
}