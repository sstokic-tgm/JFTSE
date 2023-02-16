package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SProposalAnswerRequestPacket extends Packet {
    private Integer proposalId;
    private Boolean accepted;
    private String senderName;

    public C2SProposalAnswerRequestPacket(Packet packet) {
        super(packet);

        this.proposalId = packet.readInt();
        this.accepted = packet.readBoolean();
        this.senderName = packet.readUnicodeString();
    }
}
