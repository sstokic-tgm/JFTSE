package com.jftse.emulator.server.core.life.item.special;

import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CFriendsListAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CReceivedMessageNotificationPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CYouBrokeUpWithYourCoupleAnswer;
import com.jftse.emulator.server.core.packet.packets.shop.S2CShopMoneyAnswerPacket;
import com.jftse.emulator.server.core.service.PlayerPocketService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.core.service.PocketService;
import com.jftse.emulator.server.core.service.SocialService;
import com.jftse.emulator.server.core.service.messenger.FriendService;
import com.jftse.emulator.server.core.service.messenger.MessageService;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.messenger.Message;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;

import java.util.List;

public class CoupleRing extends BaseItem {
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final SocialService socialService;
    private final FriendService friendService;
    private final MessageService messageService;
    private final PlayerService playerService;

    private Long playerCoupleId;
    private Long pocketCoupleId;

    public CoupleRing(int itemIndex, String name, String category) {
        super(itemIndex, name, category);

        pocketService = ServiceManager.getInstance().getPocketService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        socialService = ServiceManager.getInstance().getSocialService();
        friendService = ServiceManager.getInstance().getFriendService();
        messageService = ServiceManager.getInstance().getMessageService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean processPlayer(Player player) {
        player = playerService.findById(player.getId());
        if (player == null)
            return false;

        this.localPlayerId = player.getId();

        Friend playerCouple = socialService.getRelationship(player);
        if (playerCouple == null)
            return false;

        this.playerCoupleId = playerCouple.getFriend().getId();
        this.pocketCoupleId = playerCouple.getFriend().getPocket().getId();

        playerCouple.setEFriendshipState(EFriendshipState.Friends);

        Friend significantOtherCouple = socialService.getRelationship(playerCouple.getFriend());
        if (significantOtherCouple == null)
            return false;

        significantOtherCouple.setEFriendshipState(EFriendshipState.Friends);

        friendService.save(playerCouple);
        friendService.save(significantOtherCouple);

        S2CYouBrokeUpWithYourCoupleAnswer brokeUpWithYourCoupleAnswer = new S2CYouBrokeUpWithYourCoupleAnswer();
        this.packetsToSend.add(this.localPlayerId, brokeUpWithYourCoupleAnswer);

        Integer currentGold = player.getGold();
        player.setGold(currentGold - 20000);
        player = playerService.save(player);

        S2CShopMoneyAnswerPacket shopMoneyAnswerPacket = new S2CShopMoneyAnswerPacket(player);
        this.packetsToSend.add(this.localPlayerId, shopMoneyAnswerPacket);

        List<Friend> friends = socialService.getFriendList(player, EFriendshipState.Friends);
        S2CFriendsListAnswerPacket friendsListAnswerPacket = new S2CFriendsListAnswerPacket(friends);
        this.packetsToSend.add(this.localPlayerId, friendsListAnswerPacket);

        Message message = new Message();
        message.setSeen(false);
        message.setSender(player);
        message.setReceiver(playerCouple.getFriend());
        message.setMessage("[Automatic response] I divorced you");
        messageService.save(message);

        friends.clear();
        friends = socialService.getFriendList(playerCouple.getFriend(), EFriendshipState.Friends);

        friendsListAnswerPacket = new S2CFriendsListAnswerPacket(friends);
        this.packetsToSend.add(this.playerCoupleId, friendsListAnswerPacket);

        S2CReceivedMessageNotificationPacket receivedMessageNotificationPacket = new S2CReceivedMessageNotificationPacket(message);
        this.packetsToSend.add(this.playerCoupleId, receivedMessageNotificationPacket);

        return true;
    }

    @Override
    public boolean processPocket(Pocket pocket) {
        pocket = pocketService.findById(pocket.getId());
        Pocket pocketCouple = pocketService.findById(this.pocketCoupleId);
        if (pocket == null && pocketCouple == null)
            return false;

        PlayerPocket playerPocketCoupleRing = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(this.getItemIndex(), this.getCategory(), pocket);
        PlayerPocket couplePlayerPocketCoupleRing = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(this.getItemIndex(), this.getCategory(), pocketCouple);
        if (playerPocketCoupleRing == null || couplePlayerPocketCoupleRing == null)
            return false;

        handlePocketDecrement(pocket, playerPocketCoupleRing, this.localPlayerId);
        handlePocketDecrement(pocketCouple, couplePlayerPocketCoupleRing, this.playerCoupleId);

        return true;
    }

    private void handlePocketDecrement(Pocket pocket, PlayerPocket playerPocket, Long forPlayerId) {
        int itemCount = playerPocket.getItemCount() - 1;
        if (itemCount <= 0) {
            playerPocketService.remove(playerPocket.getId());
            pocketService.decrementPocketBelongings(pocket);

            S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(playerPocket.getId()));
            this.packetsToSend.add(forPlayerId, inventoryItemRemoveAnswerPacket);
        } else {
            playerPocket.setItemCount(itemCount);
            playerPocketService.save(playerPocket);
        }
    }
}
