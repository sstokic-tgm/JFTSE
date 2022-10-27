package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.Proposal;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class S2CProposalListPacket extends Packet {
    public S2CProposalListPacket(byte listType, List<Proposal> proposals) {
        super(PacketOperations.S2CProposalListAnswer.getValue());

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
