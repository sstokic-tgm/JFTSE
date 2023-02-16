package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayTeamWinsSet extends Packet {
    public S2CMatchplayTeamWinsSet(byte redTeamSets, byte blueTeamSets) {
        super(PacketOperations.S2CMatchplayTeamWinsSet.getValueAsChar());

        this.write(redTeamSets);
        this.write(blueTeamSets);
    }
}