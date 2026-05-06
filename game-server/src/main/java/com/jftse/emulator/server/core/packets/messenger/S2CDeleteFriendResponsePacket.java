package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CDeleteFriendResponsePacket extends Packet {
    public S2CDeleteFriendResponsePacket(Player deletedFriend) {
        super(PacketOperations.S2CDeleteFriendAnswer);

        this.write((byte) 1);
        this.write(deletedFriend.getId().intValue());
        this.write(deletedFriend.getName());
    }
}
