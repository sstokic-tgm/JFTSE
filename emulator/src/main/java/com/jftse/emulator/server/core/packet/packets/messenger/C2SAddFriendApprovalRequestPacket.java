package com.jftse.emulator.server.core.packet.packets.messenger;

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

        this.accept = packet.readBoolean();
        this.playerName = packet.readUnicodeString();
    }
}
