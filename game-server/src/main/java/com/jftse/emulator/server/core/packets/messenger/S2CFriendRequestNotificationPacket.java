package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class S2CFriendRequestNotificationPacket extends Packet {
    public S2CFriendRequestNotificationPacket(List<Friend> friendList) {
        super(PacketOperations.S2CFriendRequestNotification);

        this.write((byte) friendList.size());
        friendList.stream().map(Friend::getPlayer).map(Player::getName).forEach(this::write);
    }
}
