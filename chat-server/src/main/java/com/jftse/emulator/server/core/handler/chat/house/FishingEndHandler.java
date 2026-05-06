package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.life.event.GameEventBus;
import com.jftse.emulator.server.core.life.event.GameEventType;
import com.jftse.emulator.server.core.life.housing.Fish;
import com.jftse.emulator.server.core.life.housing.FishManager;
import com.jftse.emulator.server.core.life.housing.FishState;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.chat.house.S2CFishStopPacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CFishingBarPacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CHousingRewardItemPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.service.ProductService;
import com.jftse.server.core.shared.packets.chat.house.CMSGFishingEnd;

import java.util.Calendar;
import java.util.TimeZone;

@PacketId(CMSGFishingEnd.PACKET_ID)
public class FishingEndHandler implements PacketHandler<FTConnection, CMSGFishingEnd> {
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;
    private final ProductService productService;

    public FishingEndHandler() {
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.pocketService = ServiceManager.getInstance().getPocketService();
        this.productService = ServiceManager.getInstance().getProductService();
    }

    @Override
    public void handle(FTConnection connection, CMSGFishingEnd packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        Room room = client.getActiveRoom();
        RoomPlayer roomPlayer = client.getRoomPlayer();
        FTPlayer player = client.getPlayer();
        if (room == null || roomPlayer == null || !roomPlayer.getUsedRod().get())
            return;

        Pocket pocket = pocketService.findById(player.getPocketId());

        roomPlayer.getUsedRod().set(false);
        roomPlayer.setBaitX(0.0f);
        roomPlayer.setBaitY(0.0f);

        Fish claimedFish = FishManager.getInstance().getClaimedFish(room.getRoomId(), roomPlayer.getPosition());
        if (claimedFish != null) {
            Long rewardProductIndex = claimedFish.getRewardProductIndex();

            claimedFish.setState(FishState.CAUGHT);
            S2CFishStopPacket stopPacket = new S2CFishStopPacket(claimedFish.getId(), (byte) claimedFish.getState().getValue());
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(stopPacket, connection);

            FishManager.getInstance().removeFish(room.getRoomId(), claimedFish);

            S2CFishingBarPacket despawnMiniGame = new S2CFishingBarPacket(roomPlayer.getPosition(), claimedFish.getId(), 1.3f, (byte) 0);
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(despawnMiniGame, connection);

            if (rewardProductIndex != null) {
                Product item = productService.findProductByProductItemIndex(rewardProductIndex.intValue());

                PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(item.getItem0(), item.getCategory(), pocket);
                int existingItemCount = 0;
                boolean existingItem = false;

                if (playerPocket != null && !playerPocket.getUseType().equals("N/A")) {
                    existingItemCount = playerPocket.getItemCount();
                    existingItem = true;
                } else {
                    playerPocket = new PlayerPocket();
                }

                playerPocket.setCategory(item.getCategory());
                playerPocket.setItemIndex(item.getItem0());
                playerPocket.setUseType(item.getUseType());

                int quantity = 1;
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

                S2CHousingRewardItemPacket housingRewardItemPacket = new S2CHousingRewardItemPacket(playerPocket, true);
                connection.sendTCP(housingRewardItemPacket);

                GameEventBus.call(GameEventType.FISHING_SUCCESS, client, item);
            }
        }
    }
}
