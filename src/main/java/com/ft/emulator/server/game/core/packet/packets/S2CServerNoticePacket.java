package com.ft.emulator.server.game.core.packet.packets;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CServerNoticePacket extends Packet {
    public S2CServerNoticePacket(String message) {
        super(PacketID.S2CServerNotice);

        this.write(message);
    }
}