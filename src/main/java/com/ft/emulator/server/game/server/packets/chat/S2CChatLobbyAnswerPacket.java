package com.ft.emulator.server.game.server.packets.chat;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CChatLobbyAnswerPacket extends Packet {

    public S2CChatLobbyAnswerPacket(char unk, String name, String message) {

        super(PacketID.S2CChatLobbyAnswer);

        this.write(unk);
        this.write(name);
        this.write((char)0);
        this.write(message);
        this.write((char)0);
    }
}