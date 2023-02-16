package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
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