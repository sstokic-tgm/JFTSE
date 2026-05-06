package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.life.item.ItemFactory;
import com.jftse.emulator.server.core.life.item.recipe.Recipe;
import com.jftse.emulator.server.core.packets.inventory.S2CCombineNowRecipeAnswerPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.shared.packets.inventory.CMSGCombineNowRecipe;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.server.core.shared.packets.shop.SMSGSetMoney;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
@PacketId(CMSGCombineNowRecipe.PACKET_ID)
public class CombineNowRecipeHandler implements PacketHandler<FTConnection, CMSGCombineNowRecipe> {
    public CombineNowRecipeHandler() {
    }

    @Override
    public void handle(FTConnection connection, CMSGCombineNowRecipe packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        FTPlayer player = client.getPlayer();

        Recipe recipe = (Recipe) ItemFactory.getItem(packet.getPlayerPocketId(), player.getPocketId());
        if (recipe == null)
            return;

        if (!recipe.processPlayer(player)) {
            log.error("couldn't process player");
        }
        if (!recipe.processPocket(player.getPocketId())) {
            log.error("couldn't process pocket");
        }

        List<PlayerPocket> result = recipe.getResult();
        short packetResult = (short) (result.isEmpty() ? 5 : 0);
        S2CCombineNowRecipeAnswerPacket combineNowRecipeAnswerPacket = new S2CCombineNowRecipeAnswerPacket(packetResult, result);
        connection.sendTCP(combineNowRecipeAnswerPacket);

        SMSGSetMoney moneyPacket = SMSGSetMoney.builder().ap(client.getAp().get()).gold(player.getGold()).build();
        connection.sendTCP(moneyPacket);

        List<Long> itemsToRemoveFromClient = recipe.getItemsToRemoveFromClient();
        if (!itemsToRemoveFromClient.isEmpty()) {
            connection.sendTCP(itemsToRemoveFromClient.stream().map(item -> new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(item))).toArray(Packet[]::new));
        }

        List<PlayerPocket> playerPocketList = recipe.getItemsToUpdateFromClient();
        if (!playerPocketList.isEmpty()) {
            connection.sendTCP(playerPocketList.stream().map(S2CInventoryItemCountPacket::new).toArray(Packet[]::new));
        }
    }
}
