package com.ft.emulator.server.game.core.packet.packets.lobby.room;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomKickPlayerRequestPacket extends Packet {
    private short position;

    public C2SRoomKickPlayerRequestPacket(Packet packet) {
        super(packet);

        this.position = this.readShort();
    }
}