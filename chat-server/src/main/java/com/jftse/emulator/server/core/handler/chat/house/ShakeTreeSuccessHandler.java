package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.housing.FruitManager;
import com.jftse.emulator.server.core.life.housing.FruitReward;
import com.jftse.emulator.server.core.life.housing.FruitTree;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.chat.house.C2SShakeTreeSuccessPacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CHousingRewardItemPacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CShakeTreeFailPacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CShakeTreeSuccessPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.item.ItemMaterial;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;

import java.util.Calendar;
import java.util.TimeZone;

@PacketOperationIdentifier(PacketOperations.C2SShakeTreeSuccess)
public class ShakeTreeSuccessHandler extends AbstractPacketHandler {
    private C2SShakeTreeSuccessPacket shakeTreeSuccessPacket;

    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;

    public ShakeTreeSuccessHandler() {
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.pocketService = ServiceManager.getInstance().getPocketService();
    }

    @Override
    public boolean process(Packet packet) {
        shakeTreeSuccessPacket = new C2SShakeTreeSuccessPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null)
            return;

        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer == null)
            return;

        FruitManager fruitManager = client.getFruitManager();

        FruitTree fruitTree = fruitManager.pickRandomItem(shakeTreeSuccessPacket.getSong(), shakeTreeSuccessPacket.getScore());

        FruitReward fruitReward = fruitTree.getFruitReward();
        if (fruitReward == null) {
            S2CShakeTreeFailPacket shakeTreeFailPacket = new S2CShakeTreeFailPacket(roomPlayer.getPosition(), fruitTree, (short) 1);
            S2CShakeTreeSuccessPacket shakeTreeSuccessPacket = new S2CShakeTreeSuccessPacket(roomPlayer.getPosition(), fruitTree, (short) 1);
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(shakeTreeFailPacket, client.getConnection());
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(shakeTreeSuccessPacket, client.getConnection());
            return;
        }

        ItemMaterial item = fruitReward.getItem();
        if (item == null) {
            S2CShakeTreeFailPacket shakeTreeFailPacket = new S2CShakeTreeFailPacket(roomPlayer.getPosition(), fruitTree, (short) 1);
            S2CShakeTreeSuccessPacket shakeTreeSuccessPacket = new S2CShakeTreeSuccessPacket(roomPlayer.getPosition(), fruitTree, (short) 1);
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(shakeTreeFailPacket, client.getConnection());
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(shakeTreeSuccessPacket, client.getConnection());
            return;
        }

        int quantity = fruitReward.getQuantity();

        if (quantity != 0) {
            Pocket pocket = pocketService.findById(roomPlayer.getPlayer().getPocket().getId());
            PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(item.getItemIndex(), EItemCategory.MATERIAL.getName(), pocket);
            int existingItemCount = 0;
            boolean existingItem = false;

            if (playerPocket != null && !playerPocket.getUseType().equals("N/A")) {
                existingItemCount = playerPocket.getItemCount();
                existingItem = true;
            } else {
                playerPocket = new PlayerPocket();
            }

            playerPocket.setCategory(EItemCategory.MATERIAL.getName());
            playerPocket.setItemIndex(item.getItemIndex());
            playerPocket.setUseType(item.getUseType());

            if (playerPocket.getUseType().equals("N/A") && quantity > 1 && existingItemCount == 0)
                quantity = 1;

            playerPocket.setItemCount(quantity + existingItemCount);

            if (playerPocket.getUseType().equalsIgnoreCase(EItemUseType.TIME.getName())) {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                cal.add(Calendar.DAY_OF_MONTH, playerPocket.getItemCount());

                playerPocket.setCreated(cal.getTime());
            }
            playerPocket.setPocket(pocket);

            playerPocket = playerPocketService.save(playerPocket);
            if (!existingItem)
                pocket = pocketService.incrementPocketBelongings(pocket);

            S2CHousingRewardItemPacket housingRewardItemPacket = new S2CHousingRewardItemPacket(playerPocket);
            connection.sendTCP(housingRewardItemPacket);
        }
        S2CShakeTreeSuccessPacket shakeTreeSuccessPacket = new S2CShakeTreeSuccessPacket(roomPlayer.getPosition(), fruitTree, (short) 1);
        GameManager.getInstance().sendPacketToAllClientsInSameRoom(shakeTreeSuccessPacket, client.getConnection());
    }
}
