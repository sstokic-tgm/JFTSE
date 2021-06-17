package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SAddFriendApprovalRequestPacket extends Packet {
    private boolean accept;
    private String playerName;

    public C2SAddFriendApprovalRequestPacket(Packet packet) {
        super(packet);

        this.accept = this.readBoolean();
        this.playerName = this.readUnicodeString();
    }
}
