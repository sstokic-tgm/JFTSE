package com.jftse.emulator.server.core.handler.chat.house;

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
import com.jftse.entities.database.model.item.ItemMaterial;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.ItemMaterialService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;

import java.util.Calendar;
import java.util.TimeZone;

@PacketOperationIdentifier(PacketOperations.CMSG_FishingEnd)
public class FishingEndHandler extends AbstractPacketHandler {
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;
    private final ItemMaterialService itemMaterialService;

    public FishingEndHandler() {
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.pocketService = ServiceManager.getInstance().getPocketService();
        this.itemMaterialService = ServiceManager.getInstance().getItemMaterialService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null)
            return;

        Room room = client.getActiveRoom();
        RoomPlayer roomPlayer = client.getRoomPlayer();
        Player player = client.getPlayer();
        if (room == null || roomPlayer == null || player == null || !roomPlayer.getUsedRod().get())
            return;

        Pocket pocket = player.getPocket();

        roomPlayer.getUsedRod().set(false);

        Fish claimedFish = FishManager.getInstance().getClaimedFish(room.getRoomId(), roomPlayer.getPosition());
        if (claimedFish != null) {
            claimedFish.setState(FishState.CAUGHT);
            S2CFishStopPacket stopPacket = new S2CFishStopPacket(claimedFish.getId(), (byte) claimedFish.getState().getValue());
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(stopPacket, (FTConnection) connection);

            FishManager.getInstance().removeFish(room.getRoomId(), claimedFish);

            S2CFishingBarPacket despawnMiniGame = new S2CFishingBarPacket(roomPlayer.getPosition(), claimedFish.getId(), 1.3f, (byte) 0);
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(despawnMiniGame, (FTConnection) connection);

            ItemMaterial item = itemMaterialService.findByItemIndex(3).orElse(null);
            if (item == null) {
                return;
            }

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

            S2CHousingRewardItemPacket housingRewardItemPacket = new S2CHousingRewardItemPacket(playerPocket);
            connection.sendTCP(housingRewardItemPacket);
        }
    }
}
