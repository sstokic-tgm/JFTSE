package com.jftse.emulator.server.game.core.packet.packets.player;

import com.jftse.emulator.server.networking.packet.Packet;
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
