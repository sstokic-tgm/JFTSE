package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CMatchplayTeamWinsPoint extends Packet {
    public S2CMatchplayTeamWinsPoint(short scoringTeamSlot, byte pointReason, byte redTeamPoints, byte blueTeamPoints) {
        super(PacketOperations.S2CMatchplayTeamWinsPoint);

        this.write(scoringTeamSlot);
        this.write(pointReason);
        this.write(redTeamPoints);
        this.write(blueTeamPoints);
    }
}