package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CFriendRequestNotificationPacket extends Packet {
    public S2CFriendRequestNotificationPacket(String playerName) {
        super(PacketOperations.S2CFriendRequestNotification);

        this.write((byte) 1);
        this.write(playerName);
    }
}
