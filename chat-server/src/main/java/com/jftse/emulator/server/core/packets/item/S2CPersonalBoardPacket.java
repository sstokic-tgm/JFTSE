package com.jftse.emulator.server.core.packets.item;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CPersonalBoardPacket extends Packet {
    public S2CPersonalBoardPacket(String playerName, String message) {
        super(PacketOperations.S2CPersonalBoardAnswer);

        this.write(playerName);
        this.write(message);
    }
}
