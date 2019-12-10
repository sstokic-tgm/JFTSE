package com.ft.emulator.server.game.server.packets.chat;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CChatRoomAnswerPacket extends Packet {

    public S2CChatRoomAnswerPacket(byte type, String senderName, String message) {

        super(PacketID.S2CChatRoomAnswer);

        this.write(type);
        this.write(senderName);
        this.write((char)0);
        this.write(message);
        this.write((char)0);
    }
}