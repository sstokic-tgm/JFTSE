package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.database.model.messaging.Proposal;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class S2CProposalListPacket extends Packet {
    public S2CProposalListPacket(byte listType, List<Proposal> proposals) {
        super(PacketID.S2CMessengerListFiller);

        this.write(listType); // TYPE 1 = Received parcels, 1 = Sent parcels, 2 = Received proposals, 3 = Sent proposals
        this.write((byte) proposals.size());
        for (Proposal proposal : proposals) {
            // TODO: Find out packet structure
        }
    }
}
