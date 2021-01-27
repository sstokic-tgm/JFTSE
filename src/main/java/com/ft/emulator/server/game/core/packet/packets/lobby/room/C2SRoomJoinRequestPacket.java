package com.ft.emulator.server.game.core.packet.packets.lobby.room;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomJoinRequestPacket extends Packet {
    private short roomId;
    private String password;

    public C2SRoomJoinRequestPacket(Packet packet) {
        super(packet);

        this.roomId = this.readShort();
        this.readByte();
        this.password = this.readUnicodeString();
    }
}