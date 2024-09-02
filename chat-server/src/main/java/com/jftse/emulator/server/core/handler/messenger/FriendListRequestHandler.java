package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.messenger.S2CFriendRequestNotificationPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CFriendsListAnswerPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CRelationshipAnswerPacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.SocialService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SFriendListRequest)
public class FriendListRequestHandler extends AbstractPacketHandler {
    private final SocialService socialService;

    private final RProducerService rProducerService;

    public FriendListRequestHandler() {
        this.socialService = ServiceManager.getInstance().getSocialService();
        this.rProducerService = RProducerService.getInstance();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
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

        // update friend list for other online friends
        friends.stream()
                .filter(f -> f.getFriend().getOnline())
                .forEach(f -> {
                    List<Friend> onlineFriends = socialService.getFriendList(f.getFriend(), EFriendshipState.Friends);
                    S2CFriendsListAnswerPacket friendListAnswerPacket = new S2CFriendsListAnswerPacket(onlineFriends);
                    FTConnection friendConnection = GameManager.getInstance().getConnectionByPlayerId(f.getFriend().getId());
                    if (friendConnection != null) {
                        friendConnection.sendTCP(friendListAnswerPacket);
                    } else {
                        rProducerService.send("playerId", f.getFriend().getId(), friendListAnswerPacket);
                    }
                });

        List<Friend> friendsWaitingForApproval = socialService.getFriendListByFriend(player, EFriendshipState.WaitingApproval);
        S2CFriendRequestNotificationPacket s2CFriendRequestNotificationPacket = new S2CFriendRequestNotificationPacket(friendsWaitingForApproval);
        connection.sendTCP(s2CFriendRequestNotificationPacket);

        Friend myRelation = socialService.getRelationship(player);
        if (myRelation != null) {
            S2CRelationshipAnswerPacket s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(myRelation);
            connection.sendTCP(s2CRelationshipAnswerPacket);

            FTConnection friendRelationClient = GameManager.getInstance().getConnectionByPlayerId(myRelation.getFriend().getId());
            Friend friendRelation = socialService.getRelationship(myRelation.getFriend());
            if (friendRelationClient != null && friendRelation != null) {
                s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(friendRelation);
                friendRelationClient.sendTCP(s2CRelationshipAnswerPacket);
            } else if (friendRelation != null) {
                rProducerService.send("playerId", friendRelation.getPlayer().getId(), s2CRelationshipAnswerPacket);
            }
        }
    }
}
