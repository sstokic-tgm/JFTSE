package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CHousingRewardItemPacket extends Packet {
    public S2CHousingRewardItemPacket(PlayerPocket playerPocket) {
        super(PacketOperations.S2CHousingRewardItem);

        this.write(Math.toIntExact(playerPocket.getId()));
        this.write(playerPocket.getItemIndex());
        this.write(playerPocket.getItemCount());
    }

    public S2CHousingRewardItemPacket(PlayerPocket playerPocket, boolean categoryAndUseType) {
        this(playerPocket);

        if (categoryAndUseType) {
            this.write(EItemCategory.valueOf(playerPocket.getCategory()).getValue());
            this.write(playerPocket.getUseType().equals("N/A") ? (byte) 0 : EItemUseType.valueOf(playerPocket.getUseType().toUpperCase()).getValue());
        }
    }
}
