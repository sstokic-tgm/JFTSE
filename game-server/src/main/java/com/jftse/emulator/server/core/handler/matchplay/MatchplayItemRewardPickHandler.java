package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.MatchplayReward;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.core.packets.matchplay.C2SMatchplayItemRewardPickupRequest;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayItemRewardPickupAnswer;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

@PacketOperationIdentifier(PacketOperations.C2SMatchplayItemRewardPickupRequest)
public class MatchplayItemRewardPickHandler extends AbstractPacketHandler {
    private C2SMatchplayItemRewardPickupRequest packet;

    @Override
    public boolean process(Packet packet) {
        this.packet = new C2SMatchplayItemRewardPickupRequest(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null) {
            connection.close();
            return;
        }

        Player player = client.getPlayer();
        Room room = client.getActiveRoom();
        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (room == null  || roomPlayer == null) { // shouldn't happen
            connection.close();
            return;
        }

        int roomId = room.getRoomId();

        byte requestingSlot = this.packet.getSlot();
        if (GameSessionManager.getInstance().hasMatchplayReward(roomId)) {
            MatchplayReward matchplayReward = GameSessionManager.getInstance().getMatchplayReward(roomId);
            final MatchplayReward.ItemReward itemReward = matchplayReward.getSlotReward(requestingSlot);
            if (itemReward.getClaimed().compareAndSet(false, true)) {
                itemReward.setClaimedPlayerPosition(roomPlayer.getPosition());

                S2CMatchplayItemRewardPickupAnswer itemRewardPickup = new S2CMatchplayItemRewardPickupAnswer((byte) roomPlayer.getPosition(), requestingSlot, itemReward);
                GameManager.getInstance().sendPacketToAllClientsInSameRoom(itemRewardPickup, (FTConnection) connection);

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
                    Product product = ServiceManager.getInstance().getProductService().findProductByProductItemIndex(productIndex);
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
                        pocket = ServiceManager.getInstance().getPocketService().incrementPocketBelongings(pocket);

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
            } else {
                S2CMatchplayItemRewardPickupAnswer itemRewardPickup = new S2CMatchplayItemRewardPickupAnswer((byte) itemReward.getClaimedPlayerPosition(), requestingSlot, itemReward);
                connection.sendTCP(itemRewardPickup);
            }
        }
    }
}
