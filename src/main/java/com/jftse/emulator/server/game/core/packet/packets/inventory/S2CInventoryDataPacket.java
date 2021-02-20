package com.jftse.emulator.server.game.core.packet.packets.inventory;

import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.game.core.item.EItemCategory;
import com.jftse.emulator.server.game.core.item.EItemUseType;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.Date;
import java.util.List;

public class S2CInventoryDataPacket extends Packet {
    public S2CInventoryDataPacket(List<PlayerPocket> playerPocketList) {
        super(PacketID.S2CInventoryData);

        this.write((char) playerPocketList.size());

        for (PlayerPocket playerPocket : playerPocketList) {
            this.write((int) playerPocket.getId().longValue());
            this.write(EItemCategory.valueOf(playerPocket.getCategory()).getValue());
            this.write(playerPocket.getItemIndex());
            this.write(playerPocket.getUseType().equals("N/A") ? (byte) 0 : EItemUseType.valueOf(playerPocket.getUseType().toUpperCase()).getValue());
            this.write(playerPocket.getItemCount());

            long timeLeft = (playerPocket.getCreated().getTime() * 10000) - (new Date().getTime() * 10000);
            if (timeLeft <= 0) {
                timeLeft = 0;
            }
            this.write(timeLeft);

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
