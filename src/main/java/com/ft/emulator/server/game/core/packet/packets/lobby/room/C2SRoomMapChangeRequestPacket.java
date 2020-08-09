package com.ft.emulator.server.game.core.packet.packets.lobby.room;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomMapChangeRequestPacket extends Packet {
    private byte map;

    public C2SRoomMapChangeRequestPacket(Packet packet) {
        super(packet);

        this.map = this.readByte();
    }
}