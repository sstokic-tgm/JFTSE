package com.jftse.emulator.server.core.handler.game.player;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.item.EItemCategory;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.player.C2SQuickSlotUseRequestPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CFriendsListAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CReceivedMessageNotificationPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CYouBrokeUpWithYourCoupleAnswer;
import com.jftse.emulator.server.core.packet.packets.player.S2CPlayerInfoPlayStatsPacket;
import com.jftse.emulator.server.core.packet.packets.player.S2CPlayerStatusPointChangePacket;
import com.jftse.emulator.server.core.packet.packets.shop.S2CShopMoneyAnswerPacket;
import com.jftse.emulator.server.core.service.*;
import com.jftse.emulator.server.core.service.messenger.FriendService;
import com.jftse.emulator.server.core.service.messenger.MessageService;
import com.jftse.emulator.server.database.model.item.ItemChar;
import com.jftse.emulator.server.database.model.messenger.EFriendshipState;
import com.jftse.emulator.server.database.model.messenger.Friend;
import com.jftse.emulator.server.database.model.messenger.Message;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.QuickSlotEquipment;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.database.model.pocket.Pocket;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.List;

public class QuickSlotUseRequestHandler extends AbstractHandler {
    private C2SQuickSlotUseRequestPacket quickSlotUseRequestPacket;

    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final ItemCharService itemCharService;
    private final ClothEquipmentService clothEquipmentService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;
    private final FriendService friendService;
    private final SocialService socialService;
    private final MessageService messageService;

    public QuickSlotUseRequestHandler() {
        pocketService = ServiceManager.getInstance().getPocketService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        itemCharService = ServiceManager.getInstance().getItemCharService();
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        quickSlotEquipmentService = ServiceManager.getInstance().getQuickSlotEquipmentService();
        friendService = ServiceManager.getInstance().getFriendService();
        socialService = ServiceManager.getInstance().getSocialService();
        messageService = ServiceManager.getInstance().getMessageService();
    }

    @Override
    public boolean process(Packet packet) {
        quickSlotUseRequestPacket = new C2SQuickSlotUseRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Player player = connection.getClient().getPlayer();
        Pocket pocket = player.getPocket();

        PlayerPocket playerPocket = playerPocketService.getItemAsPocket((long) quickSlotUseRequestPacket.getQuickSlotId(), pocket);
        String category = playerPocket.getCategory();
        int itemIndex = playerPocket.getItemIndex();

        if (category.equals("SPECIAL") && itemIndex == 6) {
            ItemChar itemChar = itemCharService.findByPlayerType(player.getPlayerType());
            player.setStrength(itemChar.getStrength());
            player.setStamina(itemChar.getStamina());
            player.setDexterity(itemChar.getDexterity());
            player.setWillpower(itemChar.getWillpower());
            player.setStatusPoints((byte) (player.getLevel() + 5 - 1));
            connection.getClient().savePlayer(player);

            StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
            S2CPlayerStatusPointChangePacket playerStatusPointChangePacket = new S2CPlayerStatusPointChangePacket(player, statusPointsAddedDto);
            S2CPlayerInfoPlayStatsPacket playerInfoPlayStatsPacket = new S2CPlayerInfoPlayStatsPacket(player.getPlayerStatistic());
            connection.sendTCP(playerStatusPointChangePacket, playerInfoPlayStatsPacket);
        } else if (category.equals("SPECIAL") && itemIndex == 26) {
            Friend playerCouple = socialService.getRelationship(player);
            if (playerCouple == null) {
                return;
            }

            playerCouple.setEFriendshipState(EFriendshipState.Friends);

            Friend significantOtherCouple = friendService.findByPlayer(playerCouple.getFriend()).stream()
                    .filter(x -> x.getEFriendshipState().equals(EFriendshipState.Relationship))
                    .findFirst()
                    .orElse(null);
            if (significantOtherCouple == null) {
                return;
            }

            significantOtherCouple.setEFriendshipState(EFriendshipState.Friends);
            friendService.save(playerCouple);
            friendService.save(significantOtherCouple);

            S2CYouBrokeUpWithYourCoupleAnswer s2CYouBrokeUpWithYourCoupleAnswer = new S2CYouBrokeUpWithYourCoupleAnswer();
            connection.sendTCP(s2CYouBrokeUpWithYourCoupleAnswer);

            Integer currentGold = player.getGold();
            player.setGold(currentGold - 20000);
            connection.getClient().savePlayer(player);
            S2CShopMoneyAnswerPacket s2CShopMoneyAnswerPacket = new S2CShopMoneyAnswerPacket(player);
            connection.sendTCP(s2CShopMoneyAnswerPacket);

            List<Friend> friends = socialService.getFriendList(player, EFriendshipState.Friends);
            S2CFriendsListAnswerPacket s2CFriendsListAnswerPacket = new S2CFriendsListAnswerPacket(friends);
            connection.sendTCP(s2CFriendsListAnswerPacket);

            PlayerPocket item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(
                    26,
                    EItemCategory.SPECIAL.getName(),
                    playerCouple.getFriend().getPocket());
            playerPocketService.remove(item.getId());

            Message message = new Message();
            message.setSeen(false);
            message.setSender(player);
            message.setReceiver(playerCouple.getFriend());
            message.setMessage("[Automatic response] I divorced you");
            messageService.save(message);

            Client friendRelationClient = GameManager.getInstance().getClients().stream()
                    .filter(x -> x.getPlayer() != null && x.getPlayer().getId().equals(playerCouple.getFriend().getId()))
                    .findFirst()
                    .orElse(null);
            if (friendRelationClient != null) {
                friends.clear();
                friends = socialService.getFriendList(playerCouple.getFriend(), EFriendshipState.Friends);
                s2CFriendsListAnswerPacket = new S2CFriendsListAnswerPacket(friends);
                friendRelationClient.getConnection().sendTCP(s2CFriendsListAnswerPacket);

                S2CReceivedMessageNotificationPacket s2CReceivedMessageNotificationPacket = new S2CReceivedMessageNotificationPacket(message);
                S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(item.getId().intValue());
                friendRelationClient.getConnection().sendTCP(s2CReceivedMessageNotificationPacket, inventoryItemRemoveAnswerPacket);
            }
        }

        int itemCount = playerPocket.getItemCount() - 1;
        if (itemCount <= 0) {
            playerPocketService.remove(playerPocket.getId());
            pocketService.decrementPocketBelongings(pocket);

            QuickSlotEquipment quickSlotEquipment = player.getQuickSlotEquipment();
            quickSlotEquipmentService.updateQuickSlots(quickSlotEquipment, quickSlotUseRequestPacket.getQuickSlotId());

            S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(quickSlotUseRequestPacket.getQuickSlotId());
            connection.sendTCP(inventoryItemRemoveAnswerPacket);
        } else {
            playerPocket.setItemCount(itemCount);
            playerPocketService.save(playerPocket);
        }
    }
}
