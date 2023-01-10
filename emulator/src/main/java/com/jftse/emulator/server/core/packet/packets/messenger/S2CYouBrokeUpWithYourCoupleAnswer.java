package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CYouBrokeUpWithYourCoupleAnswer extends Packet {
    public S2CYouBrokeUpWithYourCoupleAnswer() {
        super(PacketOperations.S2CYouBrokeUpWithYourCoupleAnswer.getValueAsChar());

        this.write((short) 0);
    }
}
