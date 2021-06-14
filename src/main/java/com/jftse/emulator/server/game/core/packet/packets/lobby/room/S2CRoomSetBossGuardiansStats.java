package com.jftse.emulator.server.game.core.packet.packets.lobby.room;

import com.jftse.emulator.server.game.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

public class S2CRoomSetBossGuardiansStats extends Packet {
    public S2CRoomSetBossGuardiansStats(ConcurrentLinkedDeque<GuardianBattleState> guardianBattleStates) {
        super(PacketID.S2CRoomSetBossGuardiansStats);

        this.write((byte) guardianBattleStates.size());

        Iterator<GuardianBattleState> it;
        byte i;
        for (it = guardianBattleStates.iterator(), i = 0; it.hasNext(); i++)
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