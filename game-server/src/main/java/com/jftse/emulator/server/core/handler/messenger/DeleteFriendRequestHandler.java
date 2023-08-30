package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.packets.messenger.C2SDeleteFriendRequestPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CDeleteFriendResponsePacket;
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
import com.jftse.server.core.service.SocialService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SDeleteFriendRequest)
public class DeleteFriendRequestHandler extends AbstractPacketHandler {
    private C2SDeleteFriendRequestPacket c2SDeleteFriendRequestPacket;

    private final FriendService friendService;
    private final SocialService socialService;

    private final RProducerService rProducerService;

    public DeleteFriendRequestHandler() {
        friendService = ServiceManager.getInstance().getFriendService();
        socialService = ServiceManager.getInstance().getSocialService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public boolean process(Packet packet) {
        c2SDeleteFriendRequestPacket = new C2SDeleteFriendRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        Player activePlayer = ftClient.getPlayer();
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
            FTConnection friendConnection = GameManager.getInstance().getConnectionByPlayerId(friend2.getPlayer().getId());
            if (friendConnection != null) {
                friendConnection.sendTCP(targetFriendsListAnswerPacket);
            } else {
                rProducerService.send("playerId", friend2.getPlayer().getId(), targetFriendsListAnswerPacket);
            }
        }
    }
}
