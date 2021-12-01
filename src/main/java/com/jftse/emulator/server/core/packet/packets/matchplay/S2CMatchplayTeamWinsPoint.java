package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayTeamWinsPoint extends Packet {
    public S2CMatchplayTeamWinsPoint(short positionOfPointingTeam, byte ballState, byte redTeamPoints, byte blueTeamPoints) {
        super(PacketOperations.S2CMatchplayTeamWinsPoint.getValueAsChar());

        this.write(positionOfPointingTeam);
        this.write(ballState);
        this.write(redTeamPoints);
        this.write(blueTeamPoints);
    }
}