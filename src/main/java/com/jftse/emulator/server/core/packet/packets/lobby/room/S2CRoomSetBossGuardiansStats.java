package com.jftse.emulator.server.core.packet.packets.lobby.room;

import com.jftse.emulator.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

public class S2CRoomSetBossGuardiansStats extends Packet {
    public S2CRoomSetBossGuardiansStats(ConcurrentLinkedDeque<GuardianBattleState> guardianBattleStates) {
        super(PacketOperations.S2CRoomSetBossGuardiansStats.getValueAsChar());

        this.write((byte) guardianBattleStates.size());

        Iterator<GuardianBattleState> it;
        byte i;
        for (it = guardianBattleStates.iterator(), i = 0; it.hasNext(); i++)
        {
            GuardianBattleState guardianBattleState = it.next();

            this.write(i);
            this.write((short) guardianBattleState.getMaxHealth().get());
            this.write((byte) guardianBattleState.getStr().get());
            this.write((byte) guardianBattleState.getSta().get());
            this.write((byte) guardianBattleState.getDex().get());
            this.write((byte) guardianBattleState.getWill().get());
        }
    }
}