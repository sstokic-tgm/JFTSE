package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayIncreaseBreathTimerBy60Seconds extends Packet {
    public S2CMatchplayIncreaseBreathTimerBy60Seconds() {
        super(PacketOperations.S2CMatchplayIncreaseBreathTimerBy60Seconds.getValueAsChar());

        this.write((byte) 0);
    }
}