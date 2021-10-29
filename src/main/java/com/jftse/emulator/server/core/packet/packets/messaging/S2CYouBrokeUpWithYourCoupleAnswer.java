package com.jftse.emulator.server.core.packet.packets.messaging;

import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CYouBrokeUpWithYourCoupleAnswer extends Packet {
    public S2CYouBrokeUpWithYourCoupleAnswer() {
        super(PacketID.S2CYouBrokeUpWithYourCoupleAnswer);

        this.write((short) 0);
    }
}
