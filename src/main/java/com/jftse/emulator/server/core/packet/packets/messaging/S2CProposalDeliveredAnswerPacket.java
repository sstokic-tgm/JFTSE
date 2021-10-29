package com.jftse.emulator.server.core.packet.packets.messaging;

import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CProposalDeliveredAnswerPacket extends Packet {
    public S2CProposalDeliveredAnswerPacket(byte status) {
        super(PacketID.S2CProposalDeliveredAnswer);

        this.write(status);
    }
}
