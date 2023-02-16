package com.jftse.emulator.server.core.packet.packets.lobby;

import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CLobbyUserListAnswerPacket extends Packet {
    public S2CLobbyUserListAnswerPacket(List<Player> playerList) {
        super(PacketOperations.S2CLobbyUserListAnswer.getValueAsChar());

        this.write((byte) playerList.size());

        for (int i = 0; i < playerList.size(); ++i) {
            this.write((short) i);
            this.write(playerList.get(i).getName());
            this.write((int) playerList.get(i).getId().longValue());
            this.write(playerList.get(i).getPlayerType());
        }
    }
}
