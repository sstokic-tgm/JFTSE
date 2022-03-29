package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CAddFriendResponsePacket extends Packet {
    public S2CAddFriendResponsePacket(short result) {
        super(PacketOperations.S2CAddFriendAnswer.getValueAsChar());

        this.write(result);
    }
}
