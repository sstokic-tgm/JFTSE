package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayEndBasicGame extends Packet {
    public S2CMatchplayEndBasicGame(short resultTitle) {
        super(PacketID.S2CMatchplayEndBasicGame);

        this.write(resultTitle); // 0 = Loser, 1 = Winner
    }
}