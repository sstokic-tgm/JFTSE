package com.ft.emulator.server.game.core.packet.packets.chat;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CChatRoomAnswerPacket extends Packet {
    public S2CChatRoomAnswerPacket(byte type, String name, String message) {
        super(PacketID.S2CChatRoomAnswer);

        this.write(type, name, message);
    }
}
