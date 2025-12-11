package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.messenger.S2CFriendRequestNotificationPacket;
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
import com.jftse.server.core.shared.packets.messenger.CMSGAddFriend;
import com.jftse.server.core.shared.packets.messenger.SMSGAddFriend;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;

import java.util.Collections;
import java.util.List;

@PacketId(CMSGAddFriend.PACKET_ID)
public class AddFriendRequestPacketHandler implements PacketHandler<FTConnection, CMSGAddFriend> {
    private final PlayerService playerService;
    private final FriendService friendService;

    private final RProducerService rProducerService;

    public AddFriendRequestPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        friendService = ServiceManager.getInstance().getFriendService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGAddFriend packet) {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        Player player = ftClient.getPlayer();
        Player targetPlayer = playerService.findByName(packet.getPlayerName());
        if (targetPlayer == null) {
            SMSGAddFriend response = SMSGAddFriend.builder().result((short) -1).build();
            connection.sendTCP(response);
            return;
        }

        List<Friend> friends = friendService.findByPlayer(player);
        final long count = friends.stream()
                .filter(x -> x.getEFriendshipState() == EFriendshipState.Friends || x.getEFriendshipState() == EFriendshipState.Relationship)
                .count();
        if (count > 128) { // Max friends limit reached
            SMSGAddFriend response = SMSGAddFriend.builder().result((short) -2).build();
            connection.sendTCP(response);
            return;
        }

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

            SMSGAddFriend response = SMSGAddFriend.builder().result((short) 0).build();
            connection.sendTCP(response);

            S2CFriendRequestNotificationPacket s2CFriendRequestNotificationPacket = new S2CFriendRequestNotificationPacket(Collections.singletonList(friend));

            PacketMessage packetMessage = PacketMessage.builder()
                    .receivingPlayerId(targetPlayer.getId())
                    .packet(s2CFriendRequestNotificationPacket)
                    .build();
            rProducerService.send(packetMessage, "game.messenger.friendList chat.messenger.friendList", player.getName() + "(GameServer)");
            return;
        }

        if (targetFriend.getEFriendshipState() == EFriendshipState.Friends || targetFriend.getEFriendshipState() == EFriendshipState.Relationship) {
            SMSGAddFriend response = SMSGAddFriend.builder().result((short) -5).build();
            connection.sendTCP(response);
        } else if (targetFriend.getEFriendshipState() == EFriendshipState.WaitingApproval) {
            SMSGAddFriend response = SMSGAddFriend.builder().result((short) -4).build();
            connection.sendTCP(response);
        }
    }
}
