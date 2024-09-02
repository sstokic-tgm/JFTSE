package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.packets.messenger.C2SAddFriendRequestPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CAddFriendResponsePacket;
import com.jftse.emulator.server.core.packets.messenger.S2CFriendRequestNotificationPacket;
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

import java.util.Collections;
import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SAddFriendRequest)
public class AddFriendRequestPacketHandler extends AbstractPacketHandler {
    private C2SAddFriendRequestPacket c2SAddFriendRequestPacket;

    private final PlayerService playerService;
    private final FriendService friendService;

    private final RProducerService rProducerService;

    public AddFriendRequestPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        friendService = ServiceManager.getInstance().getFriendService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public boolean process(Packet packet) {
        c2SAddFriendRequestPacket = new C2SAddFriendRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        Player player = ftClient.getPlayer();
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

            S2CFriendRequestNotificationPacket s2CFriendRequestNotificationPacket = new S2CFriendRequestNotificationPacket(Collections.singletonList(friend));

            FTConnection targetConnection = GameManager.getInstance().getConnectionByPlayerId(targetPlayer.getId());
            if (targetConnection != null) {
                targetConnection.sendTCP(s2CFriendRequestNotificationPacket);
            } else {
                rProducerService.send("playerId", targetPlayer.getId(), s2CFriendRequestNotificationPacket);
            }
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
