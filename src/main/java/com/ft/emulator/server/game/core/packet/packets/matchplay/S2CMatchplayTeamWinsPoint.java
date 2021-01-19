package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CMatchplayTeamWinsPoint extends Packet {
    public S2CMatchplayTeamWinsPoint(short pointAnimation, boolean isBallOut, byte redTeamPoints, byte blueTeamPoints) {
        super(PacketID.S2CMatchplayTeamWinsPoint);

        this.write(pointAnimation);
        this.write(isBallOut);
        this.write(redTeamPoints);
        this.write(blueTeamPoints);
    }
}