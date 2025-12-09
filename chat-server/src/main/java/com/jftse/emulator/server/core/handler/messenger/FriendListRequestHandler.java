package com.jftse.emulator.server.core.handler.messenger;

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
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.SocialService;
import com.jftse.server.core.shared.packets.messenger.CMSGFriendList;

import java.util.List;

@PacketId(CMSGFriendList.PACKET_ID)
public class FriendListRequestHandler implements PacketHandler<FTConnection, CMSGFriendList> {
    private final SocialService socialService;

    private final RProducerService rProducerService;

    public FriendListRequestHandler() {
        this.socialService = ServiceManager.getInstance().getSocialService();
        this.rProducerService = RProducerService.getInstance();
    }
    @Override
    public void handle(FTConnection connection, CMSGFriendList packet) {
        FTClient ftClient = connection.getClient();
        if (ftClient == null) {
            return;
        }
        Player player = ftClient.getPlayer();
        if (player == null) {
            return;
        }

        List<Friend> friends = socialService.getFriendList(player, EFriendshipState.Friends);
        S2CFriendsListAnswerPacket s2CFriendsListAnswerPacket = new S2CFriendsListAnswerPacket(friends);
        connection.sendTCP(s2CFriendsListAnswerPacket);

        RefreshFriendListMessage refreshFriendListMessage = RefreshFriendListMessage.builder()
                .playerId(player.getId())
                .build();
        rProducerService.send(refreshFriendListMessage, "game.messenger.friendList chat.messenger.friendList", "ChatServer");

        List<Friend> friendsWaitingForApproval = socialService.getFriendListByFriend(player, EFriendshipState.WaitingApproval);
        S2CFriendRequestNotificationPacket s2CFriendRequestNotificationPacket = new S2CFriendRequestNotificationPacket(friendsWaitingForApproval);
        connection.sendTCP(s2CFriendRequestNotificationPacket);

        Friend myRelation = socialService.getRelationship(player);
        if (myRelation != null) {
            S2CRelationshipAnswerPacket s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(myRelation);
            connection.sendTCP(s2CRelationshipAnswerPacket);

            RefreshFriendRelationMessage refreshFriendRelationMessage = RefreshFriendRelationMessage.builder()
                    .playerId(player.getId())
                    .build();
            rProducerService.send(refreshFriendRelationMessage, "game.messenger.relationship chat.messenger.relationship", "ChatServer");
        }
    }
}
