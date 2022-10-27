package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CMatchplayTeamWinsSet extends Packet {
    public S2CMatchplayTeamWinsSet(byte redTeamSets, byte blueTeamSets) {
        super(PacketOperations.S2CMatchplayTeamWinsSet.getValue());

        this.write(redTeamSets);
        this.write(blueTeamSets);
    }
}