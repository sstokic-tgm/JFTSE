package com.jftse.emulator.server.game.core.packet.packets.player;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CPlayerNameCheckAnswerPacket extends Packet {
    public S2CPlayerNameCheckAnswerPacket(char result) {
        super(PacketID.S2CPlayerNameCheckAnswer);

        this.write(result);
    }
}
