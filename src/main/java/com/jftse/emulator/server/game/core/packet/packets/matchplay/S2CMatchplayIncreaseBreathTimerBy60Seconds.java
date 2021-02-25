package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayIncreaseBreathTimerBy60Seconds extends Packet {
    public S2CMatchplayIncreaseBreathTimerBy60Seconds() {
        super(PacketID.S2CMatchplayIncreaseBreathTimerBy60Seconds);
    }
}