package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.Packet;
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
