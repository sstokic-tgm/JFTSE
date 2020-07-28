package com.ft.emulator.server.game.core.packet.packets.lobby;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SLobbyUserListRequestPacket extends Packet {
    private byte page;

    public C2SLobbyUserListRequestPacket(Packet packet) {
        super(packet);

        this.page = this.readByte();
    }
}
