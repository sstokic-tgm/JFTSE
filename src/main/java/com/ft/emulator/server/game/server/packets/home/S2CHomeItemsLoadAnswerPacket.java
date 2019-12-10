package com.ft.emulator.server.game.server.packets.home;

import com.ft.emulator.server.database.model.home.HomeInventory;
import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

import java.util.List;

public class S2CHomeItemsLoadAnswerPacket extends Packet {

    public S2CHomeItemsLoadAnswerPacket(List<HomeInventory> homeInventoryList) {

        super(PacketID.S2CHomeItemsLoadAnswer);

        this.write((char)homeInventoryList.size());
        for(HomeInventory homeInventory : homeInventoryList) {

            this.write(Math.toIntExact(homeInventory.getId()));
            this.write(Math.toIntExact(homeInventory.getItemIndex()));
            this.write(homeInventory.getUnk0());
            this.write(homeInventory.getUnk1());
            this.write(homeInventory.getXPos());
            this.write(homeInventory.getYPos());
	}
    }
}