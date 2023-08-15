package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomNameChangeRequestPacket extends Packet {
    private String roomName;

    public C2SRoomNameChangeRequestPacket(Packet packet) {
        super(packet);

        this.roomName = this.readUnicodeString();
    }
}