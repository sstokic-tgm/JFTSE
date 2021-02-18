package com.jftse.emulator.server.game.core.packet.packets;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CWelcomePacket extends Packet {
    public S2CWelcomePacket(int decKey, int encKey, int decTblIdx, int encTblIdx) {
        super(PacketID.S2CLoginWelcomePacket);

        this.write(decKey);
        this.write(encKey);
        this.write(decTblIdx);
        this.write(encTblIdx);
    }
}
