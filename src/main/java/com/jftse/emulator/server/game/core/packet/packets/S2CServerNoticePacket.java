package com.jftse.emulator.server.game.core.packet.packets;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CServerNoticePacket extends Packet {
    public S2CServerNoticePacket(String message) {
        super(PacketID.S2CServerNotice);

        this.write(message);
    }
}