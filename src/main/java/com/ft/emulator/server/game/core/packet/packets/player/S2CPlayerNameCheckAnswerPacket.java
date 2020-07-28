package com.ft.emulator.server.game.core.packet.packets.player;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CPlayerNameCheckAnswerPacket extends Packet {
    public S2CPlayerNameCheckAnswerPacket(char result) {
        super(PacketID.S2CPlayerNameCheckAnswer);

        this.write(result);
    }
}
