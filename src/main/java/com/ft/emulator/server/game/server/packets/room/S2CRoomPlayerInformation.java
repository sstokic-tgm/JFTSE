package com.ft.emulator.server.game.server.packets.room;

import com.ft.emulator.server.game.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

import java.util.List;

public class S2CRoomPlayerInformation extends Packet {

    public S2CRoomPlayerInformation(List<RoomPlayer> playerList) {

        super(PacketID.S2CRoomPlayerInformation);

        this.write((short)playerList.size());

        for(RoomPlayer roomPlayer : playerList) {

            this.write(roomPlayer.getPosition());
            this.write(roomPlayer.getPlayer().getName());
            this.write((short)0);
            this.write((byte)0);
	    this.write((byte)0);
	    this.write(roomPlayer.getMaster());
	    this.write(roomPlayer.getReady());
	    this.write((byte)0); // fitting
	    this.write(roomPlayer.getPlayer().getCType());
	    this.write((byte)0);
	    this.write((byte)0);

	    this.write("");
	    this.write((short)0);

	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write((byte) 0);

	    this.write("");
	    this.write((short)0);

	    this.write(0);
	    this.write((byte)0);
	    this.write((short)0);
	    this.write((short)0);
	    this.write((short)0);
	    this.write((short)0);
	    this.write(0);

	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write(0);
	    this.write((short)0);
	}
    }
}