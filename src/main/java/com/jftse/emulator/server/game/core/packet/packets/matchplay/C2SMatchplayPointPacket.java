package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SMatchplayPointPacket extends Packet {
    private byte pointsTeam;
    private byte unk0;
    private byte ballState;
    private byte playerPosition;

    public C2SMatchplayPointPacket(Packet packet) {
        super(packet);

        this.pointsTeam = this.readByte();
        this.unk0 = this.readByte();
        this.ballState = this.readByte();
        this.playerPosition = this.readByte(); // 4 when ball state is invalid (e.g. out)
    }
}