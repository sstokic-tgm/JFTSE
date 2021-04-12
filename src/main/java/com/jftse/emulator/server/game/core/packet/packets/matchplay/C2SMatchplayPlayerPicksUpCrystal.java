package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SMatchplayPlayerPicksUpCrystal extends Packet {
    private byte playerPosition;
    private short crystalId;

    public C2SMatchplayPlayerPicksUpCrystal(Packet packet) {
        super(packet);

        this.playerPosition = this.readByte();
        this.crystalId = this.readShort();
    }
}