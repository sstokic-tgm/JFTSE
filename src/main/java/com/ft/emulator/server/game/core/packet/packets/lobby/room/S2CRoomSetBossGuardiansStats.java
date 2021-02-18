package com.ft.emulator.server.game.core.packet.packets.lobby.room;

import com.ft.emulator.server.database.model.battle.Guardian;
import com.ft.emulator.server.game.core.matchplay.battle.GuardianBattleState;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CRoomSetBossGuardiansStats extends Packet {
    public S2CRoomSetBossGuardiansStats(List<GuardianBattleState> guardianBattleStates) {
        super(PacketID.S2CRoomSetBossGuardiansStats);

        this.write((byte) guardianBattleStates.size());
        for (byte i = 0; i < guardianBattleStates.size(); i++)
        {
            GuardianBattleState guardianBattleState = guardianBattleStates.get(i);

            this.write(i);
            this.write(guardianBattleState.getMaxHealth());
            this.write(guardianBattleState.getStr());
            this.write(guardianBattleState.getSta());
            this.write(guardianBattleState.getDex());
            this.write(guardianBattleState.getWill());
        }
    }
}