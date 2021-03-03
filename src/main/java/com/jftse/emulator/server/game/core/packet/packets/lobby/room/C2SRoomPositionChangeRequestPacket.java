package com.jftse.emulator.server.game.core.packet.packets.lobby.room;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomPositionChangeRequestPacket extends Packet {
    private short position;

    public C2SRoomPositionChangeRequestPacket(Packet packet) {
        super(packet);

        this.position = this.readShort();
    }
}