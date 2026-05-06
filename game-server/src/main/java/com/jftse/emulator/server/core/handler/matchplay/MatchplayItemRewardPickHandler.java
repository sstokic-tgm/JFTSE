package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.MatchplayReward;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.matchplay.CMSGPickupItemReward;
import com.jftse.server.core.shared.packets.matchplay.SMSGPickupItemReward;
import com.jftse.server.core.thread.ThreadManager;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@PacketId(CMSGPickupItemReward.PACKET_ID)
public class MatchplayItemRewardPickHandler implements PacketHandler<FTConnection, CMSGPickupItemReward> {
    private final PocketService pocketService;

    public MatchplayItemRewardPickHandler() {
        pocketService = ServiceManager.getInstance().getPocketService();
    }

    @Override
    public void handle(FTConnection connection, CMSGPickupItemReward packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer()) {
            connection.close(); // ??
            return;
        }

        FTPlayer player = client.getPlayer();
        Room room = client.getActiveRoom();
        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (room == null  || roomPlayer == null) { // shouldn't happen
            connection.close();
            return;
        }

        int roomId = room.getRoomId();

        byte requestingSlot = packet.getSlot();

        if (GameSessionManager.getInstance().hasMatchplayReward(roomId)) {
            final MatchplayReward matchplayReward = GameSessionManager.getInstance().getMatchplayReward(roomId);
            final MatchplayReward.ItemReward itemReward = matchplayReward.getSlotReward(requestingSlot);
            if (itemReward.getClaimed().compareAndSet(false, true)) {
                itemReward.setClaimedPlayerPosition(roomPlayer.getPosition());

                SMSGPickupItemReward response = SMSGPickupItemReward.builder()
                        .playerPos((byte) roomPlayer.getPosition())
                        .slot(requestingSlot)
                        .type((byte) 0) // 0 = product, 1 = material
                        .productIndex(itemReward.getProductIndex())
                        .quantity(itemReward.getProductAmount())
                        .build();
                connection.sendTCP(response);

                notifyOtherPlayersOfNewClaim(roomPlayer, (short) roomId, requestingSlot, itemReward);

                // add reward to player pocket
                int productIndex = itemReward.getProductIndex();
                int productAmount = itemReward.getProductAmount();
                if (itemReward.getProductIndex() > 0) {
                    Product product = ServiceManager.getInstance().getProductService().findProductByProductItemIndex(productIndex);
                    if (product == null)
                        return;

                    Pocket pocket = pocketService.findById(player.getPocketId());
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
                    playerPocket.setItemCount((playerPocket.getItemCount() == null ? 0 : playerPocket.getItemCount()) + productAmount);

                    if (playerPocket.getUseType().equalsIgnoreCase(EItemUseType.TIME.getName())) {
                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        cal.add(Calendar.DAY_OF_MONTH, playerPocket.getItemCount());

                        playerPocket.setCreated(cal.getTime());
                        playerPocket.setItemCount(1);
                    }
                    playerPocket.setPocket(pocket);

                    ServiceManager.getInstance().getPlayerPocketService().save(playerPocket);
                    if (!existingItem)
                        ServiceManager.getInstance().getPocketService().incrementPocketBelongings(pocket);

                    if (!existingItem) {
                        S2CInventoryItemsPlacePacket inventoryDataPacket = new S2CInventoryItemsPlacePacket(List.of(playerPocket));
                        connection.sendTCP(inventoryDataPacket);
                    } else {
                        S2CInventoryItemCountPacket inventoryDataPacket = new S2CInventoryItemCountPacket(playerPocket);
                        connection.sendTCP(inventoryDataPacket);
                    }
                }
            } else {
                SMSGPickupItemReward response = SMSGPickupItemReward.builder()
                        .playerPos((byte) itemReward.getClaimedPlayerPosition())
                        .slot(requestingSlot)
                        .type((byte) 0) // 0 = product, 1 = material
                        .productIndex(itemReward.getProductIndex())
                        .quantity(itemReward.getProductAmount())
                        .build();
                connection.sendTCP(response);
            }

            long claimedRewardCount = matchplayReward.getSlotRewards().values().stream().filter(ir -> ir.getClaimed().get()).count();
            long activePlayerCount = room.getRoomPlayerList().stream().filter(rp -> rp.getPosition() < 4).count();

            // check if all rewards are claimed
            if (matchplayReward.getSlotRewards().values().stream().allMatch(ir -> ir.getClaimed().get()) || claimedRewardCount == activePlayerCount) {
                GameSessionManager.getInstance().removeMatchplayReward(roomId);
            }
        }
    }

    private void notifyOtherPlayersOfNewClaim(RoomPlayer roomPlayer, short roomId, byte requestingSlot, MatchplayReward.ItemReward itemReward) {
        final List<FTClient> clientsInRoom = GameManager.getInstance().getClientsInRoom(roomId);
        ThreadManager.getInstance().schedule(() -> {
            SMSGPickupItemReward response = SMSGPickupItemReward.builder()
                    .playerPos((byte) itemReward.getClaimedPlayerPosition())
                    .slot(requestingSlot)
                    .type((byte) 0) // 0 = product, 1 = material
                    .productIndex(itemReward.getProductIndex())
                    .quantity(itemReward.getProductAmount())
                    .build();
            clientsInRoom.stream()
                    .filter(c -> c.getConnection() != null && c.getRoomPlayer() != null && c.getRoomPlayer().getPosition() != roomPlayer.getPosition())
                    .map(FTClient::getConnection)
                    .forEach(c -> c.sendTCP(response));
        }, 20, TimeUnit.MILLISECONDS);
    }
}
