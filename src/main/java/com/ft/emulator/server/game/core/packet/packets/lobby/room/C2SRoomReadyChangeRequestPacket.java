package com.ft.emulator.server.game.core.packet.packets.lobby.room;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomReadyChangeRequestPacket extends Packet {
    private boolean ready;

    public C2SRoomReadyChangeRequestPacket(Packet packet) {
        super(packet);

        this.ready = this.readByte() == 1;
    }
}