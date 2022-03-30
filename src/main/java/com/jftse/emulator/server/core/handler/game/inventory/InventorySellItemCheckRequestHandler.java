package com.jftse.emulator.server.core.handler.game.inventory;

import com.jftse.emulator.common.utilities.StreamUtils;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.C2SInventorySellItemCheckReqPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventorySellAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventorySellItemAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventorySellItemCheckAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.shop.S2CShopMoneyAnswerPacket;
import com.jftse.emulator.server.core.service.PlayerPocketService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.core.service.PocketService;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.database.model.pocket.Pocket;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InventorySellItemCheckRequestHandler extends AbstractHandler {
    private C2SInventorySellItemCheckReqPacket inventorySellItemCheckReqPacket;

    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;
    private final PlayerService playerService;

    public InventorySellItemCheckRequestHandler() {
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        pocketService = ServiceManager.getInstance().getPocketService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        inventorySellItemCheckReqPacket = new C2SInventorySellItemCheckReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        byte status = S2CInventorySellItemCheckAnswerPacket.SUCCESS;
        int itemPocketId = inventorySellItemCheckReqPacket.getItemPocketId();

        Pocket pocket = connection.getClient().getPlayer().getPocket();
        PlayerPocket playerPocket = playerPocketService.getItemAsPocket((long) itemPocketId, pocket);

        if (playerPocket == null) {
            status = S2CInventorySellAnswerPacket.NO_ITEM;

            S2CInventorySellItemCheckAnswerPacket inventorySellItemCheckAnswerPacket = new S2CInventorySellItemCheckAnswerPacket(status);
            connection.sendTCP(inventorySellItemCheckAnswerPacket);
        } else {
            int sellPrice = playerPocketService.getSellPrice(playerPocket);

            S2CInventorySellItemCheckAnswerPacket inventorySellItemCheckAnswerPacket = new S2CInventorySellItemCheckAnswerPacket(status);
            connection.sendTCP(inventorySellItemCheckAnswerPacket);

            List<Integer> itemsCount = IntStream.range(0, playerPocket.getItemCount()).boxed().collect(Collectors.toList());
            StreamUtils.batches(itemsCount, 500)
                    .forEach(itemCount -> {
                        S2CInventorySellItemAnswerPacket inventorySellItemAnswerPacket = new S2CInventorySellItemAnswerPacket((char) itemCount.size(), itemPocketId);
                        connection.sendTCP(inventorySellItemAnswerPacket);
                    });

            Player player = connection.getClient().getPlayer();
            playerPocketService.remove(playerPocket.getId());
            pocket = pocketService.decrementPocketBelongings(player.getPocket());
            player.setPocket(pocket);
            player = playerService.updateMoney(player, sellPrice);

            S2CShopMoneyAnswerPacket shopMoneyAnswerPacket = new S2CShopMoneyAnswerPacket(player);
            connection.sendTCP(shopMoneyAnswerPacket);
        }
    }
}
