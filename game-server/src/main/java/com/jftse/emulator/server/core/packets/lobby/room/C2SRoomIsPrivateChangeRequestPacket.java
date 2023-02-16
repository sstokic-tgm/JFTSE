package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
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