package com.jftse.emulator.server.game.core.packet.packets.player;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CFirstPlayerAnswerPacket extends Packet {
    public S2CFirstPlayerAnswerPacket(char result, Long playerId, byte playerType) {
        super(PacketID.S2CLoginFirstPlayerAnswer);

        this.write(result);
        this.write(Math.toIntExact(playerId));
        this.write(playerType);
    }
}
