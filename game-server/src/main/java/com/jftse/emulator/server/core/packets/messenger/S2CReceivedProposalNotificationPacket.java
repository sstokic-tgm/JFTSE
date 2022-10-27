package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.Proposal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CReceivedProposalNotificationPacket extends Packet {
    public S2CReceivedProposalNotificationPacket(Proposal proposal) {
        super(PacketOperations.S2CReceivedProposalNotification.getValue());

        this.write(proposal.getId().intValue());
        this.write(proposal.getSender().getName());
        this.write(proposal.getSeen());
        this.write(proposal.getMessage());
        this.write(proposal.getCreated());
        this.write(proposal.getItemIndex());
    }
}
