package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CFriendRequestNotificationPacket extends Packet {
    public S2CFriendRequestNotificationPacket(String playerName) {
        super(PacketOperations.S2CFriendRequestNotification.getValueAsChar());

        this.write((byte) 1);
        this.write(playerName);
    }
}
