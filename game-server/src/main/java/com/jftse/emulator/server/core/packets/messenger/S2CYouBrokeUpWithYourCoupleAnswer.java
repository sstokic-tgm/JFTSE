package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CYouBrokeUpWithYourCoupleAnswer extends Packet {
    public S2CYouBrokeUpWithYourCoupleAnswer() {
        super(PacketOperations.S2CYouBrokeUpWithYourCoupleAnswer);

        this.write((short) 0);
    }
}
