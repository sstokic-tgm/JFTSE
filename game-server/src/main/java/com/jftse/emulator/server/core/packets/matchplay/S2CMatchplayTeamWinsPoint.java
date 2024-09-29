package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CMatchplayTeamWinsPoint extends Packet {
    public S2CMatchplayTeamWinsPoint(short positionOfPointingTeam, byte ballState, byte redTeamPoints, byte blueTeamPoints) {
        super(PacketOperations.S2CMatchplayTeamWinsPoint);

        this.write(positionOfPointingTeam);
        this.write(ballState);
        this.write(redTeamPoints);
        this.write(blueTeamPoints);
    }
}