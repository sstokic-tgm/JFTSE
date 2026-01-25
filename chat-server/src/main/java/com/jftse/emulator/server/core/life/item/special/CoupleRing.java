package com.jftse.emulator.server.core.life.item.special;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CFriendsListAnswerPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CReceivedMessageNotificationPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CRemoveCoupleRingPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CYouBrokeUpWithYourCoupleAnswer;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.messenger.Message;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.shop.SMSGSetMoney;

import java.util.List;

public class CoupleRing extends BaseItem {
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final SocialService socialService;
    private final FriendService friendService;
    private final MessageService messageService;
    private final PlayerService playerService;
    private final AuthenticationService authService;

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
        authService = ServiceManager.getInstance().getAuthenticationService();
    }

    @Override
    public boolean processPlayer(FTPlayer player) {
        this.localPlayerId = player.getId();
        Account account = authService.findAccountById(player.getPlayer().getAccount().getId());

        Friend playerCouple = socialService.getRelationshipWithFriend(player.getPlayerRef());
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

        int newGold = player.getGold() - 20000;
        player.syncGold(newGold);
        playerService.setMoney(player.getPlayer(), newGold);

        SMSGSetMoney moneyPacket = SMSGSetMoney.builder()
                .ap(account.getAp())
                .gold(player.getGold())
                .build();
        this.packetsToSend.add(this.localPlayerId, moneyPacket);

        List<Player> friends = socialService.getFriendList(player.getPlayerRef(), EFriendshipState.Friends).stream()
                .map(Friend::getFriend)
                .toList();
        S2CFriendsListAnswerPacket friendsListAnswerPacket = new S2CFriendsListAnswerPacket(friends);
        this.packetsToSend.add(this.localPlayerId, friendsListAnswerPacket);

        Message message = new Message();
        message.setSeen(false);
        message.setSender(player.getPlayerRef());
        message.setReceiver(playerCouple.getFriend());
        message.setMessage("[Automatic response] I divorced you");
        messageService.save(message);

        friends = socialService.getFriendList(playerCouple.getFriend(), EFriendshipState.Friends).stream()
                .map(Friend::getFriend)
                .toList();

        friendsListAnswerPacket = new S2CFriendsListAnswerPacket(friends);
        this.packetsToSend.add(this.playerCoupleId, friendsListAnswerPacket);

        S2CReceivedMessageNotificationPacket receivedMessageNotificationPacket = new S2CReceivedMessageNotificationPacket(message, player.getName());
        this.packetsToSend.add(this.playerCoupleId, receivedMessageNotificationPacket);

        return true;
    }

    @Override
    public boolean processPocket(Long pocketId) {
        Pocket pocket = pocketService.findById(pocketId);
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

            S2CRemoveCoupleRingPacket removeCoupleRing = new S2CRemoveCoupleRingPacket();
            this.packetsToSend.add(forPlayerId, removeCoupleRing);
        } else {
            playerPocket.setItemCount(itemCount);
            playerPocketService.save(playerPocket);

            S2CInventoryItemCountPacket inventoryItemCountPacket = new S2CInventoryItemCountPacket(playerPocket);
            this.packetsToSend.add(forPlayerId, inventoryItemCountPacket);
        }
    }
}
