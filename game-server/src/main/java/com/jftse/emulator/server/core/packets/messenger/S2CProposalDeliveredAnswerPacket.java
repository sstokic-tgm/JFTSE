package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CProposalDeliveredAnswerPacket extends Packet {
    public S2CProposalDeliveredAnswerPacket(byte status) {
        super(PacketOperations.S2CProposalDeliveredAnswer);

        this.write(status);
    }
}
