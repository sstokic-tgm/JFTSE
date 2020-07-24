package com.ft.emulator.server.game.core.packet.packets.lobby;

import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CLobbyUserListAnswerPacket extends Packet {

    public S2CLobbyUserListAnswerPacket(List<Player> playerList) {

	super(PacketID.S2CLobbyUserListAnswer);

	this.write((byte) playerList.size());
	for (int i = 0; i < playerList.size(); i++) {

	    this.write((short) i);
	    this.write(playerList.get(i).getName());
	    this.write((int) playerList.get(i).getId().longValue());
	    this.write(playerList.get(i).getPlayerType());
	}
    }
}