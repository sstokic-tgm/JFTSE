package com.ft.emulator.server.game.server.packets.home;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class C2SHomeItemsPlaceReqPacket extends Packet {

    private List<Map<String, Object>> homeItemsDataList;

    public C2SHomeItemsPlaceReqPacket(Packet packet) {

        super(packet);

        homeItemsDataList = new ArrayList<>();

        char size = this.readChar();
        for(int i = 0; i < size; i++) {

            Map<String, Object> homeItemsData = new HashMap<>();
            homeItemsData.put("inventoryItemId", this.readInt());
	    homeItemsData.put("unk0", this.readByte());
	    homeItemsData.put("unk1", this.readByte());
	    homeItemsData.put("unk2", this.readByte());
	    homeItemsData.put("unk3", this.readByte());
	    homeItemsData.put("itemIndex", this.readInt());
	    homeItemsData.put("unk4", this.readByte());
	    homeItemsData.put("unk5", this.readByte());
	    homeItemsData.put("xPos", this.readByte());
	    homeItemsData.put("yPos", this.readByte());
	    homeItemsDataList.add(homeItemsData);
	}
    }
}