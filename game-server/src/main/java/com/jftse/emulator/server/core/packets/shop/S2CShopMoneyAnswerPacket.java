package com.jftse.emulator.server.core.packets.shop;

import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CShopMoneyAnswerPacket extends Packet {
    public S2CShopMoneyAnswerPacket(Player player) {
        super(PacketOperations.S2CShopMoneyAnswer);

        this.write(player.getAccount().getAp());
        this.write(player.getGold());
    }
}
