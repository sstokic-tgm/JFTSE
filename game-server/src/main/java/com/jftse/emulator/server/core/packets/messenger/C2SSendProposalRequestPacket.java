package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SSendProposalRequestPacket extends Packet {
    private String receiverName;
    private String message;
    private Integer playerPocketId;
    private Integer itemIndex;

    public C2SSendProposalRequestPacket(Packet packet) {
        super(packet);

        this.receiverName = this.readString();
        this.playerPocketId = this.readInt();
        this.itemIndex = this.readInt();
        this.message = this.readString();
    }
}
