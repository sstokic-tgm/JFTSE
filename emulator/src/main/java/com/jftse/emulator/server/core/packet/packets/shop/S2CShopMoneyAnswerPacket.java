package com.jftse.emulator.server.core.packet.packets.shop;

import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CShopMoneyAnswerPacket extends Packet {
    public S2CShopMoneyAnswerPacket(Player player) {
        super(PacketOperations.S2CShopMoneyAnswer.getValueAsChar());

        this.write(player.getAccount().getAp());
        this.write(player.getGold());
    }
}
