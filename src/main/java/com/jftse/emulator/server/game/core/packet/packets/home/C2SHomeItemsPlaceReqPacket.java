package com.jftse.emulator.server.game.core.packet.packets.home;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class C2SHomeItemsPlaceReqPacket extends Packet {
    private List<Map<String, Object>> homeItemDataList;

    public C2SHomeItemsPlaceReqPacket(Packet packet) {
        super(packet);

        homeItemDataList = new ArrayList<>();

        char size = this.readChar();
        for (char i = 0; i < size; ++i) {
            Map<String, Object> homeItemData = new HashMap<>();
            homeItemData.put("inventoryItemId", this.readInt());
            homeItemData.put("homeInventoryId", this.readInt());
            homeItemData.put("itemIndex", this.readInt());
            homeItemData.put("unk0", this.readByte());
            homeItemData.put("rotation", this.readByte());
            homeItemData.put("xPos", this.readByte());
            homeItemData.put("yPos", this.readByte());
            homeItemDataList.add(homeItemData);
        }
    }
}
