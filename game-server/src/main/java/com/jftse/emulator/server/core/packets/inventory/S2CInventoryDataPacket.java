package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.List;

public class S2CInventoryDataPacket extends Packet {
    public S2CInventoryDataPacket(List<PlayerPocket> playerPocketList) {
        super(PacketOperations.S2CInventoryData);

        this.write((char) playerPocketList.size());

        for (PlayerPocket playerPocket : playerPocketList) {
            this.write((int) playerPocket.getId().longValue());
            this.write(EItemCategory.valueOf(playerPocket.getCategory()).getValue());
            this.write(playerPocket.getItemIndex());
            this.write(playerPocket.getUseType().equals("N/A") ? (byte) 0 : EItemUseType.valueOf(playerPocket.getUseType().toUpperCase()).getValue());
            this.write(playerPocket.getItemCount());
            this.write(playerPocket.getCreated());

            this.write((byte) 0); // enchant str
            this.write((byte) 0); // enchant sta
            this.write((byte) 0); // enchant dex
            this.write((byte) 0); // enchant wil
            // ??
            this.write((byte) 0);
            this.write((byte) 0);
        }
    }
}
