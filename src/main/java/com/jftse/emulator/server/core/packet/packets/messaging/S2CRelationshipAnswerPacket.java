package com.jftse.emulator.server.core.packet.packets.messaging;

import com.jftse.emulator.server.database.model.messaging.Friend;
import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CRelationshipAnswerPacket extends Packet {
    public S2CRelationshipAnswerPacket(Friend friend) {
        super(PacketID.S2CRelationshipAnswer);
            this.write(friend.getFriend().getId().intValue());
            this.write(friend.getFriend().getName());
            this.write(friend.getFriend().getPlayerType());
            this.write(friend.getFriend().getOnline() ? (short) 0 : (short) -1); // -1 offline, 0 online, 1...n game server id => game server id indicator
    }
}
