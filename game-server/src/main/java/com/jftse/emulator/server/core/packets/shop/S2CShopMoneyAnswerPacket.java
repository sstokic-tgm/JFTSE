package com.jftse.emulator.server.core.packets.shop;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;

public class S2CShopMoneyAnswerPacket extends Packet {
    public S2CShopMoneyAnswerPacket(Player player) {
        super(PacketOperations.S2CShopMoneyAnswer);

        this.write(player.getAccount().getAp());
        this.write(player.getGold());
    }
}
