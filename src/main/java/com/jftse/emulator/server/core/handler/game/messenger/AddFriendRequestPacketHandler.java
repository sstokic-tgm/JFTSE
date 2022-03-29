package com.jftse.emulator.server.core.handler.game.messenger;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.messenger.C2SAddFriendRequestPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CAddFriendResponsePacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CFriendRequestNotificationPacket;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.core.service.messenger.FriendService;
import com.jftse.emulator.server.database.model.messenger.EFriendshipState;
import com.jftse.emulator.server.database.model.messenger.Friend;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class AddFriendRequestPacketHandler extends AbstractHandler {
    private C2SAddFriendRequestPacket c2SAddFriendRequestPacket;

    private final PlayerService playerService;
    private final FriendService friendService;

    public AddFriendRequestPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        friendService = ServiceManager.getInstance().getFriendService();
    }

    @Override
    public boolean process(Packet packet) {
        c2SAddFriendRequestPacket = new C2SAddFriendRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null)
            return;

        Player player = playerService.findById(connection.getClient().getActivePlayer().getId());
        Player targetPlayer = playerService.findByName(c2SAddFriendRequestPacket.getPlayerName());
        if (targetPlayer == null) {
            S2CAddFriendResponsePacket s2CAddFriendResponsePacket = new S2CAddFriendResponsePacket((short) -1);
            connection.sendTCP(s2CAddFriendResponsePacket);
            return;
        }

        List<Friend> friends = friendService.findByPlayer(player);
        Friend targetFriend = friends.stream()
                .filter(x -> x.getFriend().getId().equals(targetPlayer.getId()))
                .findFirst()
                .orElse(null);

        if (targetFriend == null) {
            Friend friend = new Friend();
            friend.setPlayer(player);
            friend.setFriend(targetPlayer);
            friend.setEFriendshipState(EFriendshipState.WaitingApproval);
            friendService.save(friend);

            S2CAddFriendResponsePacket s2CAddFriendResponsePacket = new S2CAddFriendResponsePacket((short) 0);
            connection.sendTCP(s2CAddFriendResponsePacket);

            S2CFriendRequestNotificationPacket s2CFriendRequestNotificationPacket = new S2CFriendRequestNotificationPacket(player.getName());

            GameManager.getInstance().getClients().stream()
                    .filter(x -> x.getActivePlayer() != null && x.getActivePlayer().getId().equals(targetPlayer.getId()))
                    .findFirst()
                    .ifPresent(friendClient -> {
                        if (friendClient.getConnection() != null && friendClient.getConnection().isConnected()) {
                            friendClient.getConnection().sendTCP(s2CFriendRequestNotificationPacket);
                        }
                    });
            return;
        }

        if (targetFriend.getEFriendshipState() == EFriendshipState.Friends || targetFriend.getEFriendshipState() == EFriendshipState.Relationship) {
            S2CAddFriendResponsePacket s2CAddFriendResponsePacket = new S2CAddFriendResponsePacket((short) -5);
            connection.sendTCP(s2CAddFriendResponsePacket);
        } else if (targetFriend.getEFriendshipState() == EFriendshipState.WaitingApproval) {
            S2CAddFriendResponsePacket s2CAddFriendResponsePacket = new S2CAddFriendResponsePacket((short) -4);
            connection.sendTCP(s2CAddFriendResponsePacket);
        }
    }
}
