package com.jftse.emulator.server.game.core.packet.packets.lobby.room;

import com.jftse.emulator.server.database.model.battle.Guardian;
import com.jftse.emulator.server.game.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CRoomSetBossGuardiansStats extends Packet {
    public S2CRoomSetBossGuardiansStats(List<GuardianBattleState> guardianBattleStates) {
        super(PacketID.S2CRoomSetBossGuardiansStats);

        this.write((byte) guardianBattleStates.size());
        for (byte i = 0; i < guardianBattleStates.size(); i++)
        {
            GuardianBattleState guardianBattleState = guardianBattleStates.get(i);

            this.write(i);
            this.write((short) guardianBattleState.getMaxHealth());
            this.write((byte) guardianBattleState.getStr());
            this.write((byte) guardianBattleState.getSta());
            this.write((byte) guardianBattleState.getDex());
            this.write((byte) guardianBattleState.getWill());
        }
    }
}