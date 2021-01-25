package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SMatchplayPointPacket extends Packet {
    private byte pointsTeam;
    private byte unk0;
    private byte unk1;
    private byte unk2;

    public C2SMatchplayPointPacket(Packet packet) {
        super(packet);

        this.pointsTeam = this.readByte();
        this.unk0 = this.readByte();
        this.unk1 = this.readByte();
        this.unk2 = this.readByte(); // maybe player pos, but this had sometimes value 4
    }
}