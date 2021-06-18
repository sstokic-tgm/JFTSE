package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.messaging.Friend;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class S2CFriendsListAnswerPacket extends Packet {
    public S2CFriendsListAnswerPacket(List<Friend> friends) {
        super(PacketID.S2CFriendsListAnswer);

        this.write((byte) friends.size());
        for (Friend friend : friends) {
            this.write(friend.getFriend().getId().intValue());
            this.write(friend.getFriend().getName());
            this.write(friend.getFriend().getPlayerType());
            this.write(friend.getFriend().getOnline() ? (short) 0 : (short) -1);
        }
    }
}
