package com.jftse.emulator.server.core.packets.player;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SPlayerDeletePacket extends Packet {
    private int playerId;

    public C2SPlayerDeletePacket(Packet packet) {
        super(packet);

        this.playerId = this.readInt();
    }
}
