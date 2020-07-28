package com.ft.emulator.server.game.core.packet.packets.player;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CPlayerCreateAnswerPacket extends Packet {
    public S2CPlayerCreateAnswerPacket(char result) {
        super(PacketID.S2CPlayerCreateAnswer);

        this.write(result);
    }
}
