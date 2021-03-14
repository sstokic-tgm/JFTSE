package com.jftse.emulator.server.game.core.packet.packets.guild;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildChatAnswerPacket extends Packet {
    public S2CGuildChatAnswerPacket(String playerName, String message) {
        super(PacketID.S2CGuildChatAnswer);

        this.write(playerName, message);
    }
}
