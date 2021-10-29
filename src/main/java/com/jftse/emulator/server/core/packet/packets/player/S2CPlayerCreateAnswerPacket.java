package com.jftse.emulator.server.core.packet.packets.player;

import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CPlayerCreateAnswerPacket extends Packet {
    public S2CPlayerCreateAnswerPacket(char result) {
        super(PacketID.S2CPlayerCreateAnswer);

        this.write(result);
    }
}
