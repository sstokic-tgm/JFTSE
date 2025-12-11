package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.messenger.S2CFriendsListAnswerPacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.FriendService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.SocialService;
import com.jftse.server.core.shared.packets.messenger.CMSGAddFriendApproval;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;

import java.util.List;

@PacketId(CMSGAddFriendApproval.PACKET_ID)
public class AddFriendApprovalRequestHandler implements PacketHandler<FTConnection, CMSGAddFriendApproval> {
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
    public void handle(FTConnection connection, CMSGAddFriendApproval packet) {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        Player activePlayer = ftClient.getPlayer();
        Player targetPlayer = playerService.findByName(packet.getPlayerName());

        List<Friend> friends = friendService.findByPlayer(targetPlayer);
        final long count = friends.stream()
                .filter(x -> x.getEFriendshipState() == EFriendshipState.Friends || x.getEFriendshipState() == EFriendshipState.Relationship)
                .count();
        if (count > 128) { // Max friends limit reached
            return;
        }

        Friend friend = friends.stream()
                .filter(x -> x.getFriend().getId().equals(activePlayer.getId()))
                .findFirst()
                .orElse(null);
        if (friend == null)
            return;

        if (packet.getApproved()) {
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

            PacketMessage packetMessage = PacketMessage.builder()
                    .receivingPlayerId(targetPlayer.getId())
                    .packet(targetFriendsListAnswerPacket)
                    .build();
            rProducerService.send(packetMessage, "game.messenger.friendList chat.messenger.friendList", activePlayer.getName() + "(GameServer)");
            // TODO: ANSWER???
        } else {
            friendService.remove(friend.getId());
            // TODO: ANSWER???
        }
    }
}
