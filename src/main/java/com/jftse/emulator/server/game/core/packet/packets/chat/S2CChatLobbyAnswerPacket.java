package com.jftse.emulator.server.game.core.packet.packets.chat;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CChatLobbyAnswerPacket extends Packet {
    public S2CChatLobbyAnswerPacket(char unk, String name, String message) {
        super(PacketID.S2CChatLobbyAnswer);

        this.write(unk, name, message);
    }
}
