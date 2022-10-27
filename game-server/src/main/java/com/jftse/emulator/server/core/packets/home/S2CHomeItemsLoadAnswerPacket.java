package com.jftse.emulator.server.core.packets.home;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.entities.database.model.home.HomeInventory;

import java.util.List;

public class S2CHomeItemsLoadAnswerPacket extends Packet {
    public S2CHomeItemsLoadAnswerPacket(List<HomeInventory> homeInventoryList) {
        super(PacketOperations.S2CHomeItemsLoadAnswer.getValue());

        this.write((char) homeInventoryList.size());
        
        for (HomeInventory homeInventory : homeInventoryList) {
            this.write((int) homeInventory.getId().longValue());
            this.write(homeInventory.getItemIndex());
            this.write(homeInventory.getUnk0());
            this.write(homeInventory.getRotation());
            this.write(homeInventory.getXPos());
            this.write(homeInventory.getYPos());
        }
    }
}
