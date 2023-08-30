package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.packets.messenger.C2SAddFriendApprovalRequestPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CFriendsListAnswerPacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.FriendService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.SocialService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SAddFriendApprovalRequest)
public class AddFriendApprovalRequestHandler extends AbstractPacketHandler {
    private C2SAddFriendApprovalRequestPacket c2SAddFriendApprovalRequestPacket;

    private final PlayerService playerService;
    private final FriendService friendService;
    private final SocialService socialService;

    private final RProducerService rProducerService;

    public AddFriendApprovalRequestHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        friendService = ServiceManager.getInstance().getFriendService();
        socialService = ServiceManager.getInstance().getSocialService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public boolean process(Packet packet) {
        c2SAddFriendApprovalRequestPacket = new C2SAddFriendApprovalRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        Player activePlayer = ftClient.getPlayer();
        Player targetPlayer = playerService.findByName(c2SAddFriendApprovalRequestPacket.getPlayerName());

        List<Friend> friends = friendService.findByPlayer(targetPlayer);
        Friend friend = friends.stream()
                .filter(x -> x.getFriend().getId().equals(activePlayer.getId()))
                .findFirst()
                .orElse(null);
        if (friend == null)
            return;

        if (c2SAddFriendApprovalRequestPacket.isAccept()) {
            friend.setEFriendshipState(EFriendshipState.Friends);
            Friend newFriend = new Friend();
            newFriend.setPlayer(activePlayer);
            newFriend.setFriend(targetPlayer);
            newFriend.setEFriendshipState(EFriendshipState.Friends);

            friendService.save(friend);
            friendService.save(newFriend);

            friends.clear();
            friends = socialService.getFriendList(activePlayer, EFriendshipState.Friends);
            S2CFriendsListAnswerPacket friendsListAnswerPacket = new S2CFriendsListAnswerPacket(friends);
            GameManager.getInstance().getClients().stream()
                    .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(activePlayer.getId()))
                    .findFirst()
                    .ifPresent(c -> {
                        if (c.getConnection() != null) {
                            c.getConnection().sendTCP(friendsListAnswerPacket);
                        }
                    });

            friends.clear();
            friends = socialService.getFriendList(targetPlayer, EFriendshipState.Friends);
            S2CFriendsListAnswerPacket targetFriendsListAnswerPacket = new S2CFriendsListAnswerPacket(friends);
            FTConnection friendConnection = GameManager.getInstance().getConnectionByPlayerId(targetPlayer.getId());
            if (friendConnection != null) {
                friendConnection.sendTCP(targetFriendsListAnswerPacket);
            } else {
                rProducerService.send("playerId", targetPlayer.getId(), targetFriendsListAnswerPacket);
            }
            // TODO: ANSWER???
        } else {
            friendService.remove(friend.getId());
            // TODO: ANSWER???
        }
    }
}
