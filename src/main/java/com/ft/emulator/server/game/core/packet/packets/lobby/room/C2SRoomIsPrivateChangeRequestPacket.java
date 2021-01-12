package com.ft.emulator.server.game.core.packet.packets.lobby.room;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomIsPrivateChangeRequestPacket extends Packet {
    private String password;

    public C2SRoomIsPrivateChangeRequestPacket(Packet packet) {
        super(packet);

        this.password = this.readUnicodeString();
    }
}