package com.jftse.emulator.server.game.core.packet.packets.challenge;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SChallengePointPacket extends Packet {
    private byte pointsPlayer;
    private byte pointsNpc;

    public C2SChallengePointPacket(Packet packet) {
        super(packet);

        this.pointsPlayer = this.readByte();
        this.pointsNpc = this.readByte();
    }
}
