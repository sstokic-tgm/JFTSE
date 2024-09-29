package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CMatchplayTeamWinsSet extends Packet {
    public S2CMatchplayTeamWinsSet(byte redTeamSets, byte blueTeamSets) {
        super(PacketOperations.S2CMatchplayTeamWinsSet);

        this.write(redTeamSets);
        this.write(blueTeamSets);
    }
}