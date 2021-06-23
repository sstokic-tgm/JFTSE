package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CFriendRequestNotificationPacket extends Packet {
    public S2CFriendRequestNotificationPacket(String playerName) {
        super(PacketID.S2CFriendRequestNotification);

        this.write((byte) 1);
        this.write(playerName);
    }
}
