package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class S2CRoomSetGuardianStats extends Packet {
    public S2CRoomSetGuardianStats(ConcurrentLinkedDeque<GuardianBattleState> guardianBattleStates, List<Byte> guardians) {
        super(PacketOperations.S2CRoomSetGuardianStats);

        final ArrayList<GuardianBattleState> finalGuardianBattleStates = new ArrayList<>();

        // sort by determined guardians
        guardians.forEach(g -> {
            for (Iterator<GuardianBattleState> it = guardianBattleStates.iterator(); it.hasNext(); ) {
                GuardianBattleState guardianBattleState = it.next();

                if (g == (byte) guardianBattleState.getId())
                    finalGuardianBattleStates.add(guardianBattleState);
            }
        });

        this.write((byte) finalGuardianBattleStates.size());

        Iterator<GuardianBattleState> it;
        byte i;
        for (it = finalGuardianBattleStates.iterator(), i = 0; it.hasNext(); i++)
        {
            GuardianBattleState guardianBattleState = it.next();

            this.write(i);
            this.write((short) guardianBattleState.getMaxHealth());
            this.write((byte) guardianBattleState.getStr());
            this.write((byte) guardianBattleState.getSta());
            this.write((byte) guardianBattleState.getDex());
            this.write((byte) guardianBattleState.getWill());
        }
    }
}