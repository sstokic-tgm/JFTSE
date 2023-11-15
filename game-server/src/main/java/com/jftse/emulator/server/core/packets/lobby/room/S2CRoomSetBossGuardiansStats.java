package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.entities.database.model.battle.GuardianBase;
import com.jftse.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class S2CRoomSetBossGuardiansStats extends Packet {
    public S2CRoomSetBossGuardiansStats(ConcurrentLinkedDeque<GuardianBattleState> guardianBattleStates, List<GuardianBase> guardians) {
        super(PacketOperations.S2CRoomSetBossGuardiansStats);

        // prepare determined guardians
        final List<GuardianBase> finalGuardians = new ArrayList<>();
        finalGuardians.add(guardians.get(0));
        finalGuardians.add(guardians.get(1));
        finalGuardians.add(guardians.get(2));

        final ArrayList<GuardianBattleState> finalGuardianBattleStates = new ArrayList<>();

        // sort by determined guardians
        finalGuardians.forEach(g -> {
            if (g != null) {
                for (GuardianBattleState guardianBattleState : guardianBattleStates) {
                    if (guardianBattleState.getId() == g.getId()) {
                        finalGuardianBattleStates.add(guardianBattleState);
                    }
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