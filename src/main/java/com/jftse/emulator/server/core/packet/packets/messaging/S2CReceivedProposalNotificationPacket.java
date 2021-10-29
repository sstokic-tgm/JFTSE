package com.jftse.emulator.server.core.packet.packets.messaging;

import com.jftse.emulator.server.database.model.messaging.Proposal;
import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CReceivedProposalNotificationPacket extends Packet {
    public S2CReceivedProposalNotificationPacket(Proposal proposal) {
        super(PacketID.S2CReceivedProposalNotification);

        this.write(proposal.getId().intValue());
        this.write(proposal.getSender().getName());
        this.write(proposal.getSeen());
        this.write(proposal.getMessage());
        this.write(proposal.getCreated());
        this.write(proposal.getItemIndex());
    }
}
