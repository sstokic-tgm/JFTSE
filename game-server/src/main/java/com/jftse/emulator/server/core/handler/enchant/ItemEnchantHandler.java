package com.jftse.emulator.server.core.handler.enchant;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.enchant.C2SEnchantRequestPacket;
import com.jftse.emulator.server.core.packets.enchant.S2CEnchantAnswerPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.core.packets.shop.S2CShopMoneyAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.item.ItemEnchant;
import com.jftse.entities.database.model.item.ItemEnchantLevel;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.enchant.EnchantForge;
import com.jftse.server.core.enchant.EnchantResultMessage;
import com.jftse.server.core.enchant.EnchantingItem;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.item.EElementalKind;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.EnchantService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@PacketOperationIdentifier(PacketOperations.C2SEnchantRequest)
public class ItemEnchantHandler extends AbstractPacketHandler {
    private C2SEnchantRequestPacket enchantRequestPacket;

    private final EnchantService enchantService;
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;
    private final PlayerService playerService;

    public ItemEnchantHandler() {
        enchantService = ServiceManager.getInstance().getEnchantService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        pocketService = ServiceManager.getInstance().getPocketService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        enchantRequestPacket = new C2SEnchantRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player player = client.getPlayer();
        Pocket pocket = player.getPocket();
        if (pocket == null)
            return;

        int itemPocketId = enchantRequestPacket.getItemPocketId();
        boolean isValidPlayerPocketId = enchantService.isValidPlayerPocketId(itemPocketId, pocket);
        if (!isValidPlayerPocketId) {
            S2CEnchantAnswerPacket result = new S2CEnchantAnswerPacket(EnchantResultMessage.MSG_ITEM_ENCHANT_FAILED_02);
            connection.sendTCP(result);
            return;
        }

        int elementPocketId = enchantRequestPacket.getElementPocketId();
        int jewelPocketId = enchantRequestPacket.getJewelPocketId();
        boolean hasIngredients = enchantService.hasJewel(jewelPocketId, pocket) && enchantService.hasElemental(elementPocketId, pocket);
        if (!hasIngredients) {
            S2CEnchantAnswerPacket result = new S2CEnchantAnswerPacket(EnchantResultMessage.MSG_ITEM_ENCHANT_FAILED_03);
            connection.sendTCP(result);
            return;
        }

        if (!enchantService.isElemental(itemPocketId) && !enchantService.isEnchantable(itemPocketId)) {
            S2CEnchantAnswerPacket result = new S2CEnchantAnswerPacket(EnchantResultMessage.MSG_ITEM_ENCHANT_FAILED_07);
            connection.sendTCP(result);
            return;
        }

        PlayerPocket itemPocket = enchantService.getPlayerPocket(itemPocketId);
        PlayerPocket elementPocket = enchantService.getPlayerPocket(elementPocketId);
        PlayerPocket jewelPocket = enchantService.getPlayerPocket(jewelPocketId);
        final int currentEnchantLevel = itemPocket.getEnchantLevel();

        ItemEnchant itemJewel = enchantService.getItemEnchant(jewelPocketId);
        ItemEnchant itemEnchant = enchantService.getItemEnchant(elementPocketId);
        if (itemEnchant == null) {
            S2CEnchantAnswerPacket result = new S2CEnchantAnswerPacket(EnchantResultMessage.MSG_ITEM_ENCHANT_FAILED_01);
            connection.sendTCP(result);
            return;
        }

        int grade = currentEnchantLevel + 1;
        if (itemPocket.getEnchantElement().byteValue() != EElementalKind.valueOf(itemEnchant.getElementalKind().toUpperCase()).getValue() && itemPocket.getEnchantLevel() > 0) {
            grade = 1;
        }

        ItemEnchantLevel itemEnchantLevel = enchantService.getItemEnchantLevel(itemEnchant.getElementalKind(), grade);
        if (itemJewel == null || itemEnchantLevel == null) {
            S2CEnchantAnswerPacket result = new S2CEnchantAnswerPacket(EnchantResultMessage.MSG_ITEM_ENCHANT_FAILED_01);
            connection.sendTCP(result);
            return;
        }

        final int costs = itemEnchantLevel.getRequireGold();
        if (player.getGold() < costs) {
            S2CEnchantAnswerPacket result = new S2CEnchantAnswerPacket(EnchantResultMessage.MSG_ITEM_ENCHANT_FAILED_06);
            connection.sendTCP(result);
            return;
        }

        List<EElementalKind> elementals = Stream.of(EElementalKind.values())
                .filter(e -> e.getValue() >= 5 && e.getValue() <= 8)
                .toList();
        if (elementals.contains(EElementalKind.valueOf(itemEnchant.getElementalKind().toUpperCase()))) {
            if (enchantService.isMaxEnchantLevel(itemPocketId, true, EElementalKind.valueOf(itemEnchant.getElementalKind().toUpperCase()))) {
                S2CEnchantAnswerPacket result = new S2CEnchantAnswerPacket(EnchantResultMessage.MSG_ITEM_ENCHANT_FAILED_05);
                connection.sendTCP(result);
                return;
            }
        } else {
            if (enchantService.isMaxEnchantLevel(itemPocketId, false, EElementalKind.valueOf(itemEnchant.getElementalKind().toUpperCase()))) {
                S2CEnchantAnswerPacket result = new S2CEnchantAnswerPacket(EnchantResultMessage.MSG_ITEM_ENCHANT_FAILED_05);
                connection.sendTCP(result);
                return;
            }
        }

        EnchantForge enchantForge = new EnchantForge();
        EnchantingItem enchantingItem = enchantForge.createEnchantingItem(itemJewel, itemEnchantLevel, grade != 1 ? currentEnchantLevel : 0);

        player = playerService.setMoney(player, player.getGold() - costs);

        S2CShopMoneyAnswerPacket moneyAnswerPacket = new S2CShopMoneyAnswerPacket(player);
        connection.sendTCP(moneyAnswerPacket);

        consumeIngredients(pocket, elementPocket);
        consumeIngredients(pocket, jewelPocket);

        int newEnchantLevel = enchantForge.enchantItem(enchantingItem);
        if (newEnchantLevel <= currentEnchantLevel && grade != 1) {
            if (elementals.contains(EElementalKind.valueOf(itemEnchant.getElementalKind().toUpperCase()))) {
                if (itemPocket.getEnchantElement().byteValue() == EElementalKind.valueOf(itemEnchant.getElementalKind().toUpperCase()).getValue()) {
                    itemPocket.setEnchantLevel(newEnchantLevel);
                } else {
                    itemPocket.setEnchantLevel(1);
                    itemPocket.setEnchantElement((int) EElementalKind.valueOf(itemEnchant.getElementalKind().toUpperCase()).getValue());
                }
            }
            itemPocket = playerPocketService.save(itemPocket);

            List<PlayerPocket> playerPocketList = new ArrayList<>();
            playerPocketList.add(itemPocket);

            S2CEnchantAnswerPacket result = new S2CEnchantAnswerPacket(EnchantResultMessage.MSG_ITEM_ENCHANT_FAILED_01);
            connection.sendTCP(result);

            S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(playerPocketList);
            connection.sendTCP(inventoryDataPacket);
        } else {
            if (elementals.contains(EElementalKind.valueOf(itemEnchant.getElementalKind().toUpperCase()))) {
                itemPocket.setEnchantElement((int) EElementalKind.valueOf(itemEnchant.getElementalKind().toUpperCase()).getValue());
                itemPocket.setEnchantLevel(newEnchantLevel);
            } else {
                int elementalKind = EElementalKind.valueOf(itemEnchant.getElementalKind().toUpperCase()).getValue();
                switch (elementalKind) {
                    case 1 -> itemPocket.setEnchantStr(itemPocket.getEnchantStr() + 1);
                    case 2 -> itemPocket.setEnchantSta(itemPocket.getEnchantSta() + 1);
                    case 3 -> itemPocket.setEnchantDex(itemPocket.getEnchantDex() + 1);
                    case 4 -> itemPocket.setEnchantWil(itemPocket.getEnchantWil() + 1);
                }
            }
            itemPocket = playerPocketService.save(itemPocket);

            List<PlayerPocket> playerPocketList = new ArrayList<>();
            playerPocketList.add(itemPocket);

            S2CEnchantAnswerPacket result = new S2CEnchantAnswerPacket(EnchantResultMessage.MSG_ITEM_ENCHANT_SUCCESS);
            connection.sendTCP(result);

            S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(playerPocketList);
            connection.sendTCP(inventoryDataPacket);
        }
    }

    private void consumeIngredients(Pocket pocket, PlayerPocket pp) {
        int itemCountJewel = pp.getItemCount() - 1;
        if (itemCountJewel <= 0) {
            playerPocketService.remove(pp.getId());
            pocketService.decrementPocketBelongings(pocket);

            S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(pp.getId()));
            connection.sendTCP(inventoryItemRemoveAnswerPacket);
        } else {
            pp.setItemCount(itemCountJewel);
            playerPocketService.save(pp);

            List<PlayerPocket> playerPocketList = new ArrayList<>();
            playerPocketList.add(pp);

            S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(playerPocketList);
            connection.sendTCP(inventoryDataPacket);
        }
    }
}
