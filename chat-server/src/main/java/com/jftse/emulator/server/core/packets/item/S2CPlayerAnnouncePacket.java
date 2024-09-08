package com.jftse.emulator.server.core.packets.item;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CPlayerAnnouncePacket extends Packet {
    public S2CPlayerAnnouncePacket(String playerName, byte textSize, byte textColor, String message) {
        super(PacketOperations.S2CPlayerAnnounce);

        this.write(playerName);
        this.write(textSize);
        this.write(textColor);
        this.write(message);
    }
}
