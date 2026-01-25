package com.jftse.server.core.shared.rabbit.messages;

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
    private IPacket packet;
    private Long receivingPlayerId;

    @Builder
    public PacketMessage(IPacket packet, Long receivingPlayerId) {
        this.packet = packet;
        this.receivingPlayerId = receivingPlayerId;
    }

    @Override
    public String getMessageType() {
        return "PACKET_ONLY";
    }
}
