package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.MatchplayReward;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayItemRewardPickupAnswer;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.service.ProductService;
import com.jftse.server.core.thread.AbstractTask;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class AutoItemRewardPickerTask extends AbstractTask {
    private final FTClient client;
    private final short roomId;

    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;
    private final ProductService productService;

    public AutoItemRewardPickerTask(FTClient client, short roomId) {
        this.client = client;
        this.roomId = roomId;

        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.pocketService = ServiceManager.getInstance().getPocketService();
        this.productService = ServiceManager.getInstance().getProductService();
    }

    @Override
    public void run() {
        FTConnection connection = client.getConnection();
        Player player = client.getPlayer();
        Room room = client.getActiveRoom();
        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (room == null || roomPlayer == null || player == null) { // shouldn't happen
            connection.close();
            return;
        }

        if (GameSessionManager.getInstance().hasMatchplayReward(roomId)) {
            MatchplayReward matchplayReward = GameSessionManager.getInstance().getMatchplayReward(roomId);
            Map<Byte, MatchplayReward.ItemReward> slotRewards = matchplayReward.getSlotRewards();

            boolean rewardClaimed = false;
            while (!rewardClaimed) {
                List<Map.Entry<Byte, MatchplayReward.ItemReward>> unclaimedRewards = slotRewards.entrySet().stream()
                        .filter(entry -> !entry.getValue().getClaimed().get())
                        .toList();

                if (unclaimedRewards.isEmpty()) {
                    if (GameSessionManager.getInstance().hasMatchplayReward(roomId)) {
                        GameSessionManager.getInstance().removeMatchplayReward(roomId);
                    }
                    break;
                }

                int randomIndex = (int) (Math.random() * unclaimedRewards.size());
                Map.Entry<Byte, MatchplayReward.ItemReward> selectedRewardEntry = unclaimedRewards.get(randomIndex);
                byte requestingSlot = selectedRewardEntry.getKey();
                MatchplayReward.ItemReward itemReward = selectedRewardEntry.getValue();

                if (itemReward.getClaimed().compareAndSet(false, true)) {
                    rewardClaimed = true;

                    S2CMatchplayItemRewardPickupAnswer itemRewardPickup = new S2CMatchplayItemRewardPickupAnswer((byte) roomPlayer.getPosition(), requestingSlot, itemReward);
                    GameManager.getInstance().sendPacketToAllClientsInSameRoom(itemRewardPickup, connection);

                    long claimedRewardCount = matchplayReward.getSlotRewards().values().stream().filter(ir -> ir.getClaimed().get()).count();
                    long activePlayerCount = room.getRoomPlayerList().stream().filter(rp -> rp.getPosition() < 4).count();

                    // check if all rewards are claimed
                    if (matchplayReward.getSlotRewards().values().stream().allMatch(ir -> ir.getClaimed().get()) || claimedRewardCount == activePlayerCount) {
                        GameSessionManager.getInstance().removeMatchplayReward(roomId);
                    }

                    // add reward to player pocket
                    int productIndex = itemReward.getProductIndex();
                    int productAmount = itemReward.getProductAmount();
                    if (itemReward.getProductIndex() > 0) {
                        Product product = productService.findProductByProductItemIndex(productIndex);
                        if (product == null)
                            return;

                        Pocket pocket = player.getPocket();
                        PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(product.getItem0(), product.getCategory(), pocket);
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
                        playerPocket.setItemCount((playerPocket.getItemCount() == null ? 0 : playerPocket.getItemCount()) + productAmount);

                        if (playerPocket.getUseType().equalsIgnoreCase(EItemUseType.TIME.getName())) {
                            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            cal.add(Calendar.DAY_OF_MONTH, playerPocket.getItemCount());

                            playerPocket.setCreated(cal.getTime());
                            playerPocket.setItemCount(1);
                        }
                        playerPocket.setPocket(pocket);

                        playerPocketService.save(playerPocket);
                        if (!existingItem)
                            pocket = pocketService.incrementPocketBelongings(pocket);

                        player.setPocket(pocket);
                        client.savePlayer(player);

                        if (!existingItem) {
                            S2CInventoryItemsPlacePacket inventoryDataPacket = new S2CInventoryItemsPlacePacket(List.of(playerPocket));
                            connection.sendTCP(inventoryDataPacket);
                        } else {
                            S2CInventoryItemCountPacket inventoryDataPacket = new S2CInventoryItemCountPacket(playerPocket);
                            connection.sendTCP(inventoryDataPacket);
                        }
                    }
                }
            }
        }
    }
}
