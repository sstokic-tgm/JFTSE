package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CDeleteFriendResponsePacket extends Packet {
    public S2CDeleteFriendResponsePacket(Player deletedFriend) {
        super(PacketOperations.S2CDeleteFriendAnswer.getValueAsChar());

        this.write((byte) 1);
        this.write(deletedFriend.getId().intValue());
        this.write(deletedFriend.getName());
    }
}
