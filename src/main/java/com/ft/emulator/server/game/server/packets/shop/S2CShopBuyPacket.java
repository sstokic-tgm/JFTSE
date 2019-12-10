package com.ft.emulator.server.game.server.packets.shop;

import com.ft.emulator.server.database.model.pocket.CharacterPlayerPocket;
import com.ft.emulator.server.game.item.EItemCategory;
import com.ft.emulator.server.game.item.EItemUseType;
import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

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

    public S2CShopBuyPacket(short result, CharacterPlayerPocket characterPlayerPocket) {

        super(PacketID.S2CShopBuyAnswer);

        this.write(result);

        if(result == SUCCESS && characterPlayerPocket != null) {
            this.write((char) 1);

            this.write(Math.toIntExact(characterPlayerPocket.getId()));
            this.write(EItemCategory.valueOf(characterPlayerPocket.getCategory()).getValue());
            this.write(Math.toIntExact(characterPlayerPocket.getItemIndex()));
            this.write(characterPlayerPocket.getUseType().equals("N/A") ? (byte) 0 : EItemUseType.valueOf(characterPlayerPocket.getUseType().toUpperCase()).getValue());
            this.write(characterPlayerPocket.getItemCount());

            // ??
            this.write(0);
            this.write(0);
            this.write((byte) 0);
            this.write((byte) 0);
            this.write((byte) 0);
            this.write((byte) 0);
            this.write((byte) 0);
            this.write((byte) 0);
        }
    }
}