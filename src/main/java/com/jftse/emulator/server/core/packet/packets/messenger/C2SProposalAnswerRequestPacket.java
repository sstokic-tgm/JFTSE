package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.networking.packet.Packet;
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
