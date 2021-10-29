package com.jftse.emulator.server.core.packet.packets.messaging;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CDeleteFriendResponsePacket extends Packet {
    public S2CDeleteFriendResponsePacket(Player deletedFriend) {
        super(PacketID.S2CDeleteFriendAnswer);

        this.write((byte) 1);
        this.write(deletedFriend.getId().intValue());
        this.write(deletedFriend.getName());
    }
}
