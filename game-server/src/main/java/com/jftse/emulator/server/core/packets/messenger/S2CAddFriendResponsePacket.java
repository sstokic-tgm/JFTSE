package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CAddFriendResponsePacket extends Packet {
    public S2CAddFriendResponsePacket(short result) {
        super(PacketOperations.S2CAddFriendAnswer);

        this.write(result);
    }
}
