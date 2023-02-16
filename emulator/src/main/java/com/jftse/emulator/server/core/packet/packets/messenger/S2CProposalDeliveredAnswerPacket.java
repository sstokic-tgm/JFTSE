package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CProposalDeliveredAnswerPacket extends Packet {
    public S2CProposalDeliveredAnswerPacket(byte status) {
        super(PacketOperations.S2CProposalDeliveredAnswer.getValueAsChar());

        this.write(status);
    }
}
