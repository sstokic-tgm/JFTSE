package com.jftse.emulator.server.game.core.packet.packets.player;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CPlayerDeleteAnswerPacket extends Packet {
    public S2CPlayerDeleteAnswerPacket(char result) {
        super(PacketID.S2CPlayerDelete);

        this.write(result);
    }
}
