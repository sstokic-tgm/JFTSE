package com.ft.emulator.server.game.core.packet.packets.player;

import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CShopMoneyAnswerPacket extends Packet {
    public S2CShopMoneyAnswerPacket(Player player) {
        super(PacketID.S2CShopMoneyAnswer);

        this.write(player.getAccount().getAp());
        this.write(player.getGold());
    }
}
