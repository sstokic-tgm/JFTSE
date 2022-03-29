package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.networking.packet.Packet;
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
