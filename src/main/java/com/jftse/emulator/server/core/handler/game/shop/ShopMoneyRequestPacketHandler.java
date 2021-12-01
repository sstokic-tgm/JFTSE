package com.jftse.emulator.server.core.handler.game.shop;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.shop.S2CShopMoneyAnswerPacket;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class ShopMoneyRequestPacketHandler extends AbstractHandler {
    private final PlayerService playerService;

    public ShopMoneyRequestPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        Player player = playerService.findByIdFetched(connection.getClient().getActivePlayer().getId());
        connection.getClient().setActivePlayer(player);

        S2CShopMoneyAnswerPacket shopMoneyAnswerPacket = new S2CShopMoneyAnswerPacket(player);
        connection.sendTCP(shopMoneyAnswerPacket);
    }
}
