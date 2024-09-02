package com.jftse.emulator.server.core.matchplay;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.battle.WillDamage;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.item.EItemUseType;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@Setter
public abstract class MatchplayGame {
    protected AtomicReference<Date> startTime;
    protected AtomicReference<Date> endTime;
    protected AtomicBoolean finished;
    protected ConcurrentLinkedDeque<ScheduledFuture<?>> scheduledFutures;

    protected List<WillDamage> willDamages;

    private MatchplayHandleable handleable;

    protected MatchplayGame() {
        this.handleable = this.createHandler();
    }

    public long getTimeNeeded() {
        return endTime.get().getTime() - startTime.get().getTime();
    }

    public boolean isRedTeam(int playerPos) {
        return playerPos == 0 || playerPos == 2;
    }

    public boolean isBlueTeam(int playerPos) {
        return playerPos == 1 || playerPos == 3;
    }

    public abstract List<?> getPlayerRewards();
    public abstract void addBonusesToRewards(ConcurrentLinkedDeque<RoomPlayer> roomPlayers, List<PlayerReward> playerRewards);
    public void addRewardItemToPocket(FTClient client, PlayerReward playerReward) {
        if (client == null)
            return;

        Player player = client.getPlayer();
        if (player == null)
            return;

        if (playerReward.getProductIndex() < 0)
            return;

        Product product = ServiceManager.getInstance().getProductService().findProductByProductItemIndex(playerReward.getProductIndex());
        if (product == null)
            return;

        Pocket pocket = player.getPocket();
        PlayerPocket playerPocket = ServiceManager.getInstance().getPlayerPocketService().getItemAsPocketByItemIndexAndCategoryAndPocket(product.getItem0(), product.getCategory(), pocket);
        boolean existingItem = false;

        if (playerPocket != null && !playerPocket.getUseType().equals("N/A")) {
            existingItem = true;
        } else {
            playerPocket = new PlayerPocket();
        }

        playerPocket.setCategory(product.getCategory());
        playerPocket.setItemIndex(product.getItem0());
        playerPocket.setUseType(product.getUseType());

        // no idea how itemCount can be null here, but ok
        playerPocket.setItemCount((playerPocket.getItemCount() == null ? 0 : playerPocket.getItemCount()) + playerReward.getProductAmount());

        if (playerPocket.getUseType().equalsIgnoreCase(EItemUseType.TIME.getName())) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.add(Calendar.DAY_OF_MONTH, playerPocket.getItemCount());

            playerPocket.setCreated(cal.getTime());
            playerPocket.setItemCount(1);
        }
        playerPocket.setPocket(pocket);

        ServiceManager.getInstance().getPlayerPocketService().save(playerPocket);
        if (!existingItem)
            pocket = ServiceManager.getInstance().getPocketService().incrementPocketBelongings(pocket);

        player.setPocket(pocket);
        client.savePlayer(player);

        List<PlayerPocket> playerPocketList = new ArrayList<>();
        playerPocketList.add(playerPocket);

        S2CInventoryItemsPlacePacket inventoryDataPacket = new S2CInventoryItemsPlacePacket(playerPocketList);
        client.getConnection().sendTCP(inventoryDataPacket);
    }

    protected abstract MatchplayHandleable createHandler();
}
