package com.ft.emulator.server.game.server.packets.room;

import com.ft.emulator.server.game.matchplay.room.Room;
import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CRoomInformation extends Packet {

    public S2CRoomInformation(Room room) {

        super(PacketID.S2CRoomInformation);

        this.write(room.getId());
        this.write(room.getName());
        this.write((short)0);
        this.write((byte)0); // battlemon
	this.write(room.getGameMode());
	this.write(room.getBetting());
	this.write(room.getBettingMode());
	this.write(room.getBettingCoins());
	this.write(room.getBettingGold());
	this.write(room.getMaxPlayers());
	this.write(room.getIsPrivate());
	this.write(room.getLevel());
	this.write(room.getLevelRange());
	this.write((byte)0); // unknown
	this.write(room.getMap());
	this.write((byte)0); // unknown
	this.write((byte)0); // unknown
	this.write(room.getBall());
    }
}