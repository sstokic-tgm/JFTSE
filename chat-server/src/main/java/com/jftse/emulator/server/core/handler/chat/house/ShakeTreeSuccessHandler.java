package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.event.GameEventBus;
import com.jftse.emulator.server.core.life.event.GameEventType;
import com.jftse.emulator.server.core.life.housing.FruitManager;
import com.jftse.emulator.server.core.life.housing.FruitReward;
import com.jftse.emulator.server.core.life.housing.FruitTree;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.chat.house.S2CHousingRewardItemPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.item.ItemMaterial;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.chat.house.CMSGShakeTreeSuccess;
import com.jftse.server.core.shared.packets.chat.house.SMSGShakeTreeFail;
import com.jftse.server.core.shared.packets.chat.house.SMSGShakeTreeSuccess;

import java.util.Calendar;
import java.util.TimeZone;

@PacketId(CMSGShakeTreeSuccess.PACKET_ID)
public class ShakeTreeSuccessHandler implements PacketHandler<FTConnection, CMSGShakeTreeSuccess> {
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;

    public ShakeTreeSuccessHandler() {
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.pocketService = ServiceManager.getInstance().getPocketService();
    }

    @Override
    public void handle(FTConnection connection, CMSGShakeTreeSuccess shakeTreeSuccessPacket) {
        FTClient client = connection.getClient();
        if (client == null)
            return;

        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer == null)
            return;

        FruitManager fruitManager = client.getFruitManager();

        FruitTree fruitTree = fruitManager.pickRandomItem(shakeTreeSuccessPacket.getSong(), shakeTreeSuccessPacket.getScore());

        FruitReward fruitReward = fruitTree.getFruitReward();
        if (fruitReward == null) {
            SMSGShakeTreeFail fail = SMSGShakeTreeFail.builder()
                    .position(roomPlayer.getPosition())
                    .x(fruitTree.getX())
                    .y(fruitTree.getY())
                    .unk0((short) 1)
                    .build();
            SMSGShakeTreeSuccess success = SMSGShakeTreeSuccess.builder()
                    .position(roomPlayer.getPosition())
                    .x(fruitTree.getX())
                    .y(fruitTree.getY())
                    .unk0((short) 1)
                    .build();
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(fail, client.getConnection());
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(success, client.getConnection());
            return;
        }

        ItemMaterial item = fruitReward.getItem();
        if (item == null) {
            SMSGShakeTreeFail fail = SMSGShakeTreeFail.builder()
                    .position(roomPlayer.getPosition())
                    .x(fruitTree.getX())
                    .y(fruitTree.getY())
                    .unk0((short) 1)
                    .build();
            SMSGShakeTreeSuccess success = SMSGShakeTreeSuccess.builder()
                    .position(roomPlayer.getPosition())
                    .x(fruitTree.getX())
                    .y(fruitTree.getY())
                    .unk0((short) 1)
                    .build();
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(fail, client.getConnection());
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(success, client.getConnection());
            return;
        }

        int quantity = fruitReward.getQuantity();

        if (quantity != 0) {
            Pocket pocket = pocketService.findById(roomPlayer.getPocketId());
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
                playerPocket.setItemCount(1);
            }
            playerPocket.setPocket(pocket);

            playerPocket = playerPocketService.save(playerPocket);
            if (!existingItem)
                pocket = pocketService.incrementPocketBelongings(pocket);

            S2CHousingRewardItemPacket housingRewardItemPacket = new S2CHousingRewardItemPacket(playerPocket);
            connection.sendTCP(housingRewardItemPacket);
        }

        SMSGShakeTreeSuccess success = SMSGShakeTreeSuccess.builder()
                .position(roomPlayer.getPosition())
                .x(fruitTree.getX())
                .y(fruitTree.getY())
                .unk0((short) 1)
                .build();
        GameManager.getInstance().sendPacketToAllClientsInSameRoom(success, connection);

        GameEventBus.call(GameEventType.TREE_SHAKE_SUCCESS, client, item);
    }
}
