package com.jftse.emulator.server.game.core.packet.packets.lobby.room;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomGameModeChangeRequestPacket extends Packet {
    private byte mode;

    public C2SRoomGameModeChangeRequestPacket(Packet packet) {
        super(packet);

        this.mode = this.readByte();
    }
}