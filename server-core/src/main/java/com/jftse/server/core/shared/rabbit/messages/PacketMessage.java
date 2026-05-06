package com.jftse.server.core.shared.rabbit.messages;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.rabbit.AbstractBaseMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PacketMessage extends AbstractBaseMessage {
    /*@JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = JsonTypeInfo.As.PROPERTY,
            property = "@class"
    )
    private IPacket packet;*/
    private byte[] packet;
    private int packetId;

    private Long receivingPlayerId;

    @Builder
    public PacketMessage(IPacket packet, Long receivingPlayerId) {
        this.packet = packet.toBytes();
        this.packetId = packet.getPacketId();
        this.receivingPlayerId = receivingPlayerId;
    }

    @Override
    public String getMessageType() {
        return "PACKET_ONLY";
    }
}
