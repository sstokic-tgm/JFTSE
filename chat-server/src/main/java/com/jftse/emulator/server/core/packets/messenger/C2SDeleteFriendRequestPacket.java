package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SDeleteFriendRequestPacket extends Packet {
    private Integer friendId;

    public C2SDeleteFriendRequestPacket(Packet packet) {
        super(packet);

        this.friendId = packet.readInt();
    }
}
