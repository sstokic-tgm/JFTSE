package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SAddFriendApprovalRequestPacket extends Packet {
    private boolean accept;
    private String playerName;

    public C2SAddFriendApprovalRequestPacket(Packet packet) {
        super(packet);

        this.accept = packet.readBoolean();
        this.playerName = packet.readUnicodeString();
    }
}
