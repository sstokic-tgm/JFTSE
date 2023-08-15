package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomFittingRequestPacket extends Packet {
    private boolean fitting;

    public C2SRoomFittingRequestPacket(Packet packet) {
        super(packet);

        this.fitting = this.readByte() != 0;
    }
}