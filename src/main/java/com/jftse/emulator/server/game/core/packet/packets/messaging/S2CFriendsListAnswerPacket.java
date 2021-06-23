package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.database.model.messaging.Friend;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
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
            this.write(friend.getFriend().getOnline() ? (short) 0 : (short) -1); // -1 offline, 0 online, 1...n game server id => game server id indicator
        }
    }
}
