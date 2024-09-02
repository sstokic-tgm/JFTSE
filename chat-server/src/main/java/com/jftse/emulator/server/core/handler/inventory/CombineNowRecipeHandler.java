package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.life.item.ItemFactory;
import com.jftse.emulator.server.core.life.item.recipe.Recipe;
import com.jftse.emulator.server.core.packets.inventory.C2SCombineNowRecipeReqPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CCombineNowRecipeAnswerPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.emulator.server.core.packets.shop.S2CShopMoneyAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
@PacketOperationIdentifier(PacketOperations.C2SCombineNowRecipe)
public class CombineNowRecipeHandler extends AbstractPacketHandler {
    private C2SCombineNowRecipeReqPacket combineNowRecipeReqPacket;

    public CombineNowRecipeHandler() {
    }

    @Override
    public boolean process(Packet packet) {
        combineNowRecipeReqPacket = new C2SCombineNowRecipeReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player player = client.getPlayer();

        Pocket pocket = client.getPlayer().getPocket();
        if (pocket == null)
            return;

        Recipe recipe = (Recipe) ItemFactory.getItem(combineNowRecipeReqPacket.getPlayerPocketId(), pocket);
        if (recipe == null)
            return;

        if (!recipe.processPlayer(player)) {
            log.error("couldn't process player");
        }
        if (!recipe.processPocket(pocket)) {
            log.error("couldn't process pocket");
        }
        player = client.getPlayer();

        List<PlayerPocket> result = recipe.getResult();
        short packetResult = (short) (result.isEmpty() ? 5 : 0);
        S2CCombineNowRecipeAnswerPacket combineNowRecipeAnswerPacket = new S2CCombineNowRecipeAnswerPacket(packetResult, result);
        connection.sendTCP(combineNowRecipeAnswerPacket);

        S2CShopMoneyAnswerPacket shopMoneyAnswerPacket = new S2CShopMoneyAnswerPacket(player);
        connection.sendTCP(shopMoneyAnswerPacket);

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
