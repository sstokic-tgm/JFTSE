package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SMessageSeenRequestPacket extends Packet {
    private Byte unk0;
    private Integer messageId;

    public C2SMessageSeenRequestPacket(Packet packet) {
        super(packet);

        this.unk0 = packet.readByte();
        this.messageId = packet.readInt();
    }
}
