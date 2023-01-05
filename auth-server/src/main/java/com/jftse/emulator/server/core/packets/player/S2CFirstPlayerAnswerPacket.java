package com.jftse.emulator.server.core.packets.player;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CFirstPlayerAnswerPacket extends Packet {
    public S2CFirstPlayerAnswerPacket(char result, Long playerId, byte playerType) {
        super(PacketOperations.S2CLoginFirstPlayerAnswer);

        this.write(result);
        this.write(Math.toIntExact(playerId));
        this.write(playerType);
    }
}
