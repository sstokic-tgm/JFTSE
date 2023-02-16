package com.jftse.emulator.server.core.packet.packets.player;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CPlayerDeleteAnswerPacket extends Packet {
    public S2CPlayerDeleteAnswerPacket(char result) {
        super(PacketOperations.S2CPlayerDelete.getValueAsChar());

        this.write(result);
    }
}
