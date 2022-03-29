package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayEndBasicGame extends Packet {
    public S2CMatchplayEndBasicGame(short resultTitle) {
        super(PacketOperations.S2CMatchplayEndBasicGame.getValueAsChar());

        this.write(resultTitle); // 0 = Loser, 1 = Winner
    }
}