package com.jftse.emulator.server.core.handler.game.messenger;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.messenger.C2SAddFriendApprovalRequestPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CFriendsListAnswerPacket;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.core.service.SocialService;
import com.jftse.emulator.server.core.service.messenger.FriendService;
import com.jftse.emulator.server.database.model.messenger.EFriendshipState;
import com.jftse.emulator.server.database.model.messenger.Friend;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class AddFriendApprovalRequestHandler extends AbstractHandler {
    private C2SAddFriendApprovalRequestPacket c2SAddFriendApprovalRequestPacket;

    private final PlayerService playerService;
    private final FriendService friendService;
    private final SocialService socialService;

    public AddFriendApprovalRequestHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        friendService = ServiceManager.getInstance().getFriendService();
        socialService = ServiceManager.getInstance().getSocialService();
    }

    @Override
    public boolean process(Packet packet) {
        c2SAddFriendApprovalRequestPacket = new C2SAddFriendApprovalRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        Player activePlayer = connection.getClient().getPlayer();
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
                        if (c.getConnection() != null && c.getConnection().isConnected()) {
                            c.getConnection().sendTCP(friendsListAnswerPacket);
                        }
                    });

            friends.clear();
            friends = socialService.getFriendList(targetPlayer, EFriendshipState.Friends);
            S2CFriendsListAnswerPacket targetFriendsListAnswerPacket = new S2CFriendsListAnswerPacket(friends);
            GameManager.getInstance().getClients().stream()
                    .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(targetPlayer.getId()))
                    .findFirst()
                    .ifPresent(c -> {
                        if (c.getConnection() != null && c.getConnection().isConnected()) {
                            c.getConnection().sendTCP(targetFriendsListAnswerPacket);
                        }
                    });
            // TODO: ANSWER???
        } else {
            friendService.remove(friend.getId());
            // TODO: ANSWER???
        }
    }
}
