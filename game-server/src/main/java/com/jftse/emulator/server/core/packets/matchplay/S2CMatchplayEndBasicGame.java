package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CMatchplayEndBasicGame extends Packet {
    public S2CMatchplayEndBasicGame(byte resultTitle) {
        super(PacketOperations.S2CMatchplayEndBasicGame);

        this.write(resultTitle); // 0 = Loser, 1 = Winner
    }
}