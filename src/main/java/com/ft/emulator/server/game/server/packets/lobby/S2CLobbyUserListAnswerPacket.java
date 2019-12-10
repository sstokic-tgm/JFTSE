package com.ft.emulator.server.game.server.packets.lobby;

import com.ft.emulator.server.database.model.character.CharacterPlayer;
import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

import java.util.List;

public class S2CLobbyUserListAnswerPacket extends Packet {

    public S2CLobbyUserListAnswerPacket(List<CharacterPlayer> characterPlayerList) {

        super(PacketID.S2CLobbyUserListAnswer);

        this.write((byte)characterPlayerList.size());
        for(int i = 0; i < characterPlayerList.size(); i++) {

            this.write((short)i);
            this.write(characterPlayerList.get(i).getName());
            this.write((short)0);
            this.write(Math.toIntExact(characterPlayerList.get(i).getId()));
            this.write(characterPlayerList.get(i).getCType());
	}
    }
}