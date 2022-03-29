package com.jftse.emulator.server.core.packet.packets.player;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CFirstPlayerAnswerPacket extends Packet {
    public S2CFirstPlayerAnswerPacket(char result, Long playerId, byte playerType) {
        super(PacketOperations.S2CLoginFirstPlayerAnswer.getValueAsChar());

        this.write(result);
        this.write(Math.toIntExact(playerId));
        this.write(playerType);
    }
}
