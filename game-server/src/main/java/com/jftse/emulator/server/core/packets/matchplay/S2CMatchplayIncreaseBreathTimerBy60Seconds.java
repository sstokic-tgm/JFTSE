package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CMatchplayIncreaseBreathTimerBy60Seconds extends Packet {
    public S2CMatchplayIncreaseBreathTimerBy60Seconds() {
        super(PacketOperations.S2CMatchplayIncreaseBreathTimerBy60Seconds);

        this.write((byte) 0);
    }
}