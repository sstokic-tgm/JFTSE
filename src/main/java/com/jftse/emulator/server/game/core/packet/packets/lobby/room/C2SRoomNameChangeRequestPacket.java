package com.jftse.emulator.server.game.core.packet.packets.lobby.room;

import com.jftse.emulator.server.networking.packet.Packet;
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