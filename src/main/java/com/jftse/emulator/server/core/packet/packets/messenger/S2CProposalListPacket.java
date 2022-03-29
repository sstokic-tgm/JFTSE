package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.database.model.messenger.Proposal;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class S2CProposalListPacket extends Packet {
    public S2CProposalListPacket(byte listType, List<Proposal> proposals) {
        super(PacketOperations.S2CProposalListAnswer.getValueAsChar());

        this.write(listType);
        this.write((byte) proposals.size());
        for (Proposal proposal : proposals) {
            this.write(proposal.getId().intValue());
            this.write(listType == (byte) 0 ? proposal.getSender().getName() : proposal.getReceiver().getName());
            this.write(proposal.getSeen());
            this.write(proposal.getMessage());
            this.write(proposal.getCreated());
            this.write(proposal.getItemIndex());
        }
    }
}
