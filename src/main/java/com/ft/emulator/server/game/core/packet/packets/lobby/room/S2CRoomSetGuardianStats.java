package com.ft.emulator.server.game.core.packet.packets.lobby.room;

import com.ft.emulator.server.database.model.battle.Guardian;
import com.ft.emulator.server.game.core.matchplay.battle.GuardianBattleState;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CRoomSetGuardianStats extends Packet {
    public S2CRoomSetGuardianStats(List<GuardianBattleState> guardianBattleStates) {
        super(PacketID.S2CRoomSetGuardianStats);

        this.write((byte) guardianBattleStates.size());
        for (byte i = 0; i < guardianBattleStates.size(); i++)
        {
            GuardianBattleState guardianBattleState = guardianBattleStates.get(i);
            Guardian guardian = guardianBattleState.getGuardian();

            this.write(i);
            this.write(guardian.getHpBase().shortValue());
            this.write(guardian.getBaseStr());
            this.write(guardian.getBaseSta());
            this.write(guardian.getBaseDex());
            this.write(guardian.getBaseWill());
        }
    }
}