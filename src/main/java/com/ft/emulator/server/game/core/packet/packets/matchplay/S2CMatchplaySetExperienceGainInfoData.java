package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CMatchplaySetExperienceGainInfoData extends Packet {
    public S2CMatchplaySetExperienceGainInfoData(byte resultTitle) {
        super(PacketID.S2CMatchPlaySetExperienceGainInfoData);

        this.write(resultTitle); // 0 = Loser, 1 = Winner
    }
}