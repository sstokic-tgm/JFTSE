package com.jftse.emulator.server.game.core.packet.packets.shop;

import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.game.core.item.EItemCategory;
import com.jftse.emulator.server.game.core.item.EItemUseType;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.Date;
import java.util.List;

public class S2CShopBuyPacket extends Packet {
    public final static short SUCCESS = 0;
    public final static short NEED_MORE_GOLD = -1;
    public final static short NEED_MORE_CASH = -2;
    public final static short ERROR_CHAR_LIMIT = -3;
    public final static short ERROR_ITEM_LIMIT = -6;
    public final static short GET_CASHINFO_FAILED = -8;
    public final static short ERR_RECEIVED_GIFT_ITEM = -21;
    public final static short NOT_FOR_SALE_TO_CUR_CHAR = -31;
    public final static short NEED_MORE_CHARM_POINT = -33;
    public final static short NOT_FOR_SALE_LIMIT_PRODUCT = -34;
    public final static short INVENTORY_FULL = -98;

    public S2CShopBuyPacket(short result, List<PlayerPocket> playerPocketList) {
        super(PacketID.S2CShopBuyAnswer);

        this.write(result);

        if (result == SUCCESS && playerPocketList != null && !playerPocketList.isEmpty()) {
            this.write((char) playerPocketList.size());

            for (PlayerPocket playerPocket : playerPocketList) {
                this.write((int) playerPocket.getId().longValue());
                this.write(EItemCategory.valueOf(playerPocket.getCategory()).getValue());
                this.write(playerPocket.getItemIndex());
                this.write(playerPocket.getUseType().equals("N/A") ? (byte) 0 : EItemUseType.valueOf(playerPocket.getUseType().toUpperCase()).getValue());
                this.write(playerPocket.getItemCount());

                long timeLeft = (playerPocket.getCreated().getTime() * 10000) - (new Date().getTime() * 10000);
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
}
