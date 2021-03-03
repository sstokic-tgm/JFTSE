package com.jftse.emulator.server.game.core.packet.packets.player;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CPlayerLevelExpPacket extends Packet {
    public S2CPlayerLevelExpPacket(byte level, int expValue) {
        super(PacketID.S2CPlayerLevelExpData);

        this.write(level);
        this.write(expValue);
    }
}
