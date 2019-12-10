package com.ft.emulator.server.game.server.packets.tutorial;

import com.ft.emulator.server.game.item.EItemCategory;
import com.ft.emulator.server.game.item.EItemUseType;
import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

import java.util.List;
import java.util.Map;

public class S2CTutorialFinishPacket extends Packet {

    public S2CTutorialFinishPacket(boolean win, byte newLevel, int exp, int gold, int secondsNeeded, List<Map<String, Object>> rewardItemList) {

        super(PacketID.S2CTutorialEnd);

	this.write(win);
	this.write(newLevel);
	this.write(exp);
	this.write(gold);
	this.write(secondsNeeded);

	this.write((char)rewardItemList.size());
	for(Map<String, Object> rewardItem : rewardItemList) {

	    Long id = (Long)rewardItem.get("id");
	    String category = (String)rewardItem.get("category");
	    Long itemIndex = (Long)rewardItem.get("itemIndex");
	    String useType = (String)rewardItem.get("useType");
	    Integer itemCount = (Integer)rewardItem.get("itemCount");

	    this.write(Math.toIntExact(id));
	    this.write(EItemCategory.valueOf(category).getValue());
	    this.write(Math.toIntExact(itemIndex));
	    this.write(useType.equals("N/A") ? (byte)0 : EItemUseType.valueOf(useType.toUpperCase()).getValue());
	    this.write(itemCount);

	    // ??
	    this.write(0);
	    this.write(0);
	    this.write((byte)0);
	    this.write((byte)0);
	    this.write((byte)0);
	    this.write((byte)0);
	    this.write((byte)0);
	    this.write((byte)0);
	}
    }
}