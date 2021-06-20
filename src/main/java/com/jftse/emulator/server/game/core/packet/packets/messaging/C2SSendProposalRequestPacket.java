package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.networking.packet.Packet;
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

        this.receiverName = packet.readUnicodeString();
        this.playerPocketId = packet.readInt();
        this.itemIndex = packet.readInt();
        this.message = packet.readUnicodeString();
    }
}
