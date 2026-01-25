package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.messenger.S2CFriendRequestNotificationPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CFriendsListAnswerPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CRelationshipAnswerPacket;
import com.jftse.emulator.server.core.rabbit.messages.RefreshFriendListMessage;
import com.jftse.emulator.server.core.rabbit.messages.RefreshFriendRelationMessage;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.SocialService;
import com.jftse.server.core.shared.packets.messenger.CMSGFriendList;

import java.util.List;

@PacketId(CMSGFriendList.PACKET_ID)
public class FriendListRequestHandler implements PacketHandler<FTConnection, CMSGFriendList> {
    private final SocialService socialService;
    private final PlayerService playerService;

    private final RProducerService rProducerService;

    public FriendListRequestHandler() {
        this.socialService = ServiceManager.getInstance().getSocialService();
        this.playerService = ServiceManager.getInstance().getPlayerService();
        this.rProducerService = RProducerService.getInstance();
    }
    @Override
    public void handle(FTConnection connection, CMSGFriendList packet) {
        FTClient ftClient = connection.getClient();
        if (!ftClient.hasPlayer()) {
            return;
        }

        FTPlayer player = ftClient.getPlayer();

        List<Player> friends = socialService.getFriendList(player.getPlayerRef(), EFriendshipState.Friends).stream()
                .map(Friend::getFriend)
                .toList();
        S2CFriendsListAnswerPacket s2CFriendsListAnswerPacket = new S2CFriendsListAnswerPacket(friends);
        connection.sendTCP(s2CFriendsListAnswerPacket);

        RefreshFriendListMessage refreshFriendListMessage = RefreshFriendListMessage.builder()
                .playerId(player.getId())
                .build();
        rProducerService.send(refreshFriendListMessage, "game.messenger.friendList chat.messenger.friendList", "GameServer");

        List<Player> friendsWaitingForApproval = socialService.getFriendListByFriend(player.getPlayerRef(), EFriendshipState.WaitingApproval).stream()
                .map(Friend::getFriend)
                .toList();
        S2CFriendRequestNotificationPacket s2CFriendRequestNotificationPacket = new S2CFriendRequestNotificationPacket(friendsWaitingForApproval);
        connection.sendTCP(s2CFriendRequestNotificationPacket);

        Friend myRelation = socialService.getRelationship(player.getPlayerRef());
        if (myRelation != null) {
            Player pMyRelation = playerService.findWithAccountById(myRelation.getFriend().getId());
            S2CRelationshipAnswerPacket s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(pMyRelation);
            connection.sendTCP(s2CRelationshipAnswerPacket);

            RefreshFriendRelationMessage refreshFriendRelationMessage = RefreshFriendRelationMessage.builder()
                    .playerId(player.getId())
                    .build();
            rProducerService.send(refreshFriendRelationMessage, "game.messenger.relationship chat.messenger.relationship", "GameServer");
        }
    }
}
