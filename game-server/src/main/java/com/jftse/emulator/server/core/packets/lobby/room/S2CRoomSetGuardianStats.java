package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.entities.database.model.battle.GuardianBase;
import com.jftse.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class S2CRoomSetGuardianStats extends Packet {
    public S2CRoomSetGuardianStats(ConcurrentLinkedDeque<GuardianBattleState> guardianBattleStates, List<GuardianBase> guardians) {
        super(PacketOperations.S2CRoomSetGuardianStats);

        final ArrayList<GuardianBattleState> finalGuardianBattleStates = new ArrayList<>();

        // sort by determined guardians
        guardians.forEach(g -> {
            for (GuardianBattleState guardianBattleState : guardianBattleStates) {
                if (guardianBattleState.getId() == g.getId()) {
                    finalGuardianBattleStates.add(guardianBattleState);
                }
            }
        });

        this.write((byte) finalGuardianBattleStates.size());

        for (int i = 0; i < finalGuardianBattleStates.size(); i++) {
            GuardianBattleState guardianBattleState = finalGuardianBattleStates.get(i);

            this.write((byte) i);
            this.write((short) guardianBattleState.getMaxHealth());
            this.write((byte) guardianBattleState.getStr());
            this.write((byte) guardianBattleState.getSta());
            this.write((byte) guardianBattleState.getDex());
            this.write((byte) guardianBattleState.getWill());
        }
    }
}