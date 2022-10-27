package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.Friend;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class S2CFriendsListAnswerPacket extends Packet {
    public S2CFriendsListAnswerPacket(List<Friend> friends) {
        super(PacketOperations.S2CFriendsListAnswer.getValue());

        this.write((byte) friends.size());
        for (Friend friend : friends) {
            this.write(friend.getFriend().getId().intValue());
            this.write(friend.getFriend().getName());
            this.write(friend.getFriend().getPlayerType());
            this.write(friend.getFriend().getOnline() ? (short) 0 : (short) -1); // -1 offline, 0 online, 1...n game server id => game server id indicator
        }
    }
}
