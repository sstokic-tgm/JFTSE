package com.ft.emulator.server.game.server.packets.room;

import com.ft.emulator.server.game.matchplay.room.Room;
import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

import java.util.List;

public class S2CRoomListAnswerPacket extends Packet {

    public S2CRoomListAnswerPacket(List<Room> roomList) {

        super(PacketID.S2CRoomListAnswer);

        this.write((short)roomList.size());

        for(int i = 0; i < roomList.size(); i++) {

            Room room = roomList.get(i);

            this.write((short)i);
            this.write(room.getName());
            this.write((short)0);
            this.write(room.getGameMode());
            this.write(room.getBattleMode());
            this.write((byte)0); // unknown
	    this.write((byte)0); // unknown
	    this.write((byte)0); // unknown
	    this.write(0); // unknown
	    this.write(room.getBall());
	    this.write(room.getMaxPlayers());
	    this.write(room.getIsPrivate());
	    this.write(room.getLevel());
	    this.write(room.getLevelRange());
	    this.write((byte)0); // unknown
	    this.write(room.getMap());
	    this.write((byte)0); // unknown
	    this.write((byte)0); // unknown
	    this.write((byte)room.getPlayerList().size());
	    this.write((byte)0); // unknown
	    this.write((byte)0); // unknown
	}
    }
}