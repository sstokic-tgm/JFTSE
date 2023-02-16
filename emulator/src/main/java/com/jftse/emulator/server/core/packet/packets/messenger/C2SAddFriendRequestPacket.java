package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SAddFriendRequestPacket extends Packet {
    private String playerName;

    public C2SAddFriendRequestPacket(Packet packet) {
        super(packet);

        this.playerName = packet.readUnicodeString();
    }
}
