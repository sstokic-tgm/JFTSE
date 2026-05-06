package com.jftse.emulator.server.core.packets.lobby;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.List;

public class S2CLobbyUserListAnswerPacket extends Packet {
    public S2CLobbyUserListAnswerPacket(List<FTPlayer> playerList) {
        super(PacketOperations.S2CLobbyUserListAnswer);

        this.write((byte) playerList.size());

        for (int i = 0; i < playerList.size(); ++i) {
            this.write((short) i);
            this.write(playerList.get(i).getName());
            this.write((int) playerList.get(i).getId());
            this.write((byte) playerList.get(i).getPlayerType());
        }
    }
}
