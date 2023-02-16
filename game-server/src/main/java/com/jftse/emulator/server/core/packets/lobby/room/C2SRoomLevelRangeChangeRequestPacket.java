package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomLevelRangeChangeRequestPacket extends Packet {
    private byte levelRange;

    public C2SRoomLevelRangeChangeRequestPacket(Packet packet) {
        super(packet);

        this.levelRange = this.readByte();
    }
}