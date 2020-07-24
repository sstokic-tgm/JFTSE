package com.ft.emulator.server.game.core.packet.packets.player;

import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.database.model.player.StatusPointsAddedDto;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CPlayerStatusPointChangePacket extends Packet {

    /**
     * TODO: reverse this packet structure correctly
     */
    public S2CPlayerStatusPointChangePacket(Player player, StatusPointsAddedDto statusPointsAddedDto) {

	super(PacketID.S2CPlayerStatusPointChange);

	this.write(200 + (3 * (player.getLevel() - 1))); // hp = base hp + 3hp for each level; level - 1 because at level 1 we have the base hp

	// status points
	this.write(player.getStrength());
	this.write(player.getStamina());
	this.write(player.getDexterity());
	this.write(player.getWillpower());
	// cloth added status points
	this.write(statusPointsAddedDto.getStrength());
	this.write(statusPointsAddedDto.getStamina());
	this.write(statusPointsAddedDto.getDexterity());
	this.write(statusPointsAddedDto.getWillpower());
	// ??
	for (int i = 5; i < 13; i++) {
	    this.write((byte) 0);
	}
	// ??
	this.write((byte) 0);
	this.write((byte) 0);
	// add hp
	this.write(0);
	// cloth added status points for shop
	this.write((byte) 0);
	this.write((byte) 0);
	this.write((byte) 0);
	this.write((byte) 0);
	//??
	this.write(statusPointsAddedDto.getAddHp());
	this.write((byte) 0);
	this.write((byte) 0);
	this.write((byte) 0);
	this.write((byte) 0);
	// ??
	for (int i = 5; i < 13; i++) {
	    this.write((byte) 0);
	}
	// ??
	for (int i = 5; i < 13; i++) {
	    this.write((byte) 0);
	}

	this.write(player.getStatusPoints());
    }
}