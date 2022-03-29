package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.database.model.messenger.Friend;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CRelationshipAnswerPacket extends Packet {
    public S2CRelationshipAnswerPacket(Friend friend) {
        super(PacketOperations.S2CRelationshipAnswer.getValueAsChar());
            this.write(friend.getFriend().getId().intValue());
            this.write(friend.getFriend().getName());
            this.write(friend.getFriend().getPlayerType());
            this.write(friend.getFriend().getOnline() ? (short) 0 : (short) -1); // -1 offline, 0 online, 1...n game server id => game server id indicator
    }
}
