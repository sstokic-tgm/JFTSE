package com.ft.emulator.server.game.core.packet.packets.home;

import com.ft.emulator.server.database.model.home.HomeInventory;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CHomeItemsLoadAnswerPacket extends Packet {

    public S2CHomeItemsLoadAnswerPacket(List<HomeInventory> homeInventoryList) {

	super(PacketID.S2CHomeItemsLoadAnswer);

	this.write((char) homeInventoryList.size());
	for (HomeInventory homeInventory : homeInventoryList) {

	    this.write((int) homeInventory.getId().longValue());
	    this.write(homeInventory.getItemIndex());
	    this.write(homeInventory.getUnk0());
	    this.write(homeInventory.getUnk1());
	    this.write(homeInventory.getXPos());
	    this.write(homeInventory.getYPos());
	}
    }
}