package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CMatchplayTeamWinsSet extends Packet {
    public S2CMatchplayTeamWinsSet(byte redTeamSets, byte blueTeamSets) {
        super(PacketID.S2CMatchplayTeamWinsSet);

        this.write(redTeamSets);
        this.write(blueTeamSets);
    }
}