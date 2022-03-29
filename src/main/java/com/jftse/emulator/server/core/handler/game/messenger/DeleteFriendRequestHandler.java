package com.jftse.emulator.server.core.handler.game.messenger;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.messenger.C2SDeleteFriendRequestPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CDeleteFriendResponsePacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CFriendsListAnswerPacket;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.core.service.SocialService;
import com.jftse.emulator.server.core.service.messenger.FriendService;
import com.jftse.emulator.server.database.model.messenger.EFriendshipState;
import com.jftse.emulator.server.database.model.messenger.Friend;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class DeleteFriendRequestHandler extends AbstractHandler {
    private C2SDeleteFriendRequestPacket c2SDeleteFriendRequestPacket;

    private final PlayerService playerService;
    private final FriendService friendService;
    private final SocialService socialService;

    public DeleteFriendRequestHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        friendService = ServiceManager.getInstance().getFriendService();
        socialService = ServiceManager.getInstance().getSocialService();
    }

    @Override
    public boolean process(Packet packet) {
        c2SDeleteFriendRequestPacket = new C2SDeleteFriendRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null)
            return;

        Player activePlayer = playerService.findById(connection.getClient().getActivePlayer().getId());
        Friend friend1 = friendService.findByPlayerIdAndFriendId(activePlayer.getId(), c2SDeleteFriendRequestPacket.getFriendId());
        if (friend1 != null) {
            friendService.remove(friend1.getId());
            S2CDeleteFriendResponsePacket s2CDeleteFriendResponsePacket = new S2CDeleteFriendResponsePacket(friend1.getFriend());
            connection.sendTCP(s2CDeleteFriendResponsePacket);
        }

        Friend friend2 = friendService.findByPlayerIdAndFriendId(c2SDeleteFriendRequestPacket.getFriendId(), activePlayer.getId());
        if (friend2 != null) {
            friendService.remove(friend2.getId());

            List<Friend> friends = socialService.getFriendList(friend2.getPlayer(), EFriendshipState.Friends);
            S2CFriendsListAnswerPacket targetFriendsListAnswerPacket = new S2CFriendsListAnswerPacket(friends);
            GameManager.getInstance().getClients().stream()
                    .filter(c -> c.getActivePlayer() != null && c.getActivePlayer().getId().equals(friend2.getPlayer().getId()))
                    .findFirst()
                    .ifPresent(c -> {
                        if (c.getConnection() != null && c.getConnection().isConnected()) {
                            c.getConnection().sendTCP(targetFriendsListAnswerPacket);
                        }
                    });
        }
    }
}
