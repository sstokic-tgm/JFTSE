package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemsPlacePacket;
import com.jftse.emulator.server.core.packets.messenger.S2CReceivedMessageNotificationPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CRelationshipAnswerPacket;
import com.jftse.emulator.server.core.rabbit.messages.RefreshFriendRelationMessage;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.messenger.Message;
import com.jftse.entities.database.model.messenger.Proposal;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.messenger.CMSGAcceptProposal;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;

import java.util.List;

@PacketId(CMSGAcceptProposal.PACKET_ID)
public class ProposalAnswerRequestHandler implements PacketHandler<FTConnection, CMSGAcceptProposal> {
    private final ProposalService proposalService;
    private final FriendService friendService;
    private final MessageService messageService;
    private final PlayerPocketService playerPocketService;
    private final SocialService socialService;
    private final PlayerService playerService;

    private final RProducerService rProducerService;

    public ProposalAnswerRequestHandler() {
        proposalService = ServiceManager.getInstance().getProposalService();
        friendService = ServiceManager.getInstance().getFriendService();
        messageService = ServiceManager.getInstance().getMessageService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        socialService = ServiceManager.getInstance().getSocialService();
        playerService = ServiceManager.getInstance().getPlayerService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGAcceptProposal packet) {
        FTClient ftClient = connection.getClient();
        if (!ftClient.hasPlayer())
            return;

        FTPlayer player = ftClient.getPlayer();
        Proposal proposal = proposalService.findById((long) packet.getProposalId());
        if (proposal == null) return;

        Player sendPlayer = playerService.findById(proposal.getSender().getId());
        Player receivePlayer = playerService.findById(proposal.getReceiver().getId());
        if (sendPlayer == null || receivePlayer == null) {
            proposalService.remove(proposal.getId());
            // maybe additional cleanup needed
            return;
        }

        List<Friend> senderFriend = friendService.findByPlayer(sendPlayer);
        if (senderFriend.stream().anyMatch(x -> x.getEFriendshipState().equals(EFriendshipState.Relationship))) {
            if (packet.getAccept()) {
                Message message = new Message();
                message.setSeen(false);
                message.setSender(sendPlayer);
                message.setReceiver(receivePlayer);
                message.setMessage("[Automatic response] I'm sorry but I'm already in a relationship");
                messageService.save(message);

                S2CReceivedMessageNotificationPacket s2CReceivedMessageNotificationPacket = new S2CReceivedMessageNotificationPacket(message, sendPlayer.getName());
                connection.sendTCP(s2CReceivedMessageNotificationPacket);
            }

            proposalService.remove(proposal.getId());
            return;
        }

        Message message = new Message();
        message.setSeen(false);
        message.setSender(receivePlayer);
        message.setReceiver(sendPlayer);
        if (packet.getAccept()) {
            List<Friend> receiverFriend = friendService.findByPlayer(receivePlayer);
            if (receiverFriend.stream().anyMatch(x -> x.getEFriendshipState().equals(EFriendshipState.Relationship))) {
                proposalService.remove(proposal.getId());
                return;
            }

            message.setMessage("[Automatic response] I accepted your proposal <3");

            Friend friendOfSender = friendService.findByPlayerIdAndFriendId(
                    sendPlayer.getId(),
                    receivePlayer.getId());
            if (friendOfSender == null) {
                friendOfSender = new Friend();
                friendOfSender.setPlayer(sendPlayer);
                friendOfSender.setFriend(receivePlayer);
            }

            friendOfSender.setEFriendshipState(EFriendshipState.Relationship);

            Friend friendOfReceiver = friendService.findByPlayerIdAndFriendId(
                    proposal.getReceiver().getId(),
                    proposal.getSender().getId());
            if (friendOfReceiver == null) {
                friendOfReceiver = new Friend();
                friendOfReceiver.setPlayer(receivePlayer);
                friendOfReceiver.setFriend(sendPlayer);
            }

            friendOfReceiver.setEFriendshipState(EFriendshipState.Relationship);

            friendService.save(friendOfSender);
            friendService.save(friendOfReceiver);

            Friend myRelation = socialService.getRelationship(player.getPlayerRef());
            if (myRelation != null) {
                Player pMyRelation = playerService.findWithAccountById(myRelation.getFriend().getId());
                S2CRelationshipAnswerPacket s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(pMyRelation);
                connection.sendTCP(s2CRelationshipAnswerPacket);

                RefreshFriendRelationMessage refreshFriendRelationMessage = RefreshFriendRelationMessage.builder()
                        .playerId(player.getId())
                        .build();
                rProducerService.send(refreshFriendRelationMessage, "game.messenger.relationship chat.messenger.relationship", "GameServer");
            }

            PlayerPocket senderPocket = new PlayerPocket();
            senderPocket.setPocket(sendPlayer.getPocket());
            senderPocket.setCategory(EItemCategory.SPECIAL.getName());
            senderPocket.setUseType(EItemUseType.INSTANT.getName());
            senderPocket.setItemCount(1);
            senderPocket.setItemIndex(26);
            senderPocket = playerPocketService.save(senderPocket);

            PlayerPocket receiverPocket = new PlayerPocket();
            receiverPocket.setPocket(receivePlayer.getPocket());
            receiverPocket.setCategory(EItemCategory.SPECIAL.getName());
            receiverPocket.setUseType(EItemUseType.INSTANT.getName());
            receiverPocket.setItemCount(1);
            receiverPocket.setItemIndex(26);
            receiverPocket = playerPocketService.save(receiverPocket);

            S2CInventoryItemsPlacePacket senderInventoryPacket = new S2CInventoryItemsPlacePacket(List.of(senderPocket));
            S2CInventoryItemsPlacePacket receiverInventoryPacket = new S2CInventoryItemsPlacePacket(List.of(receiverPocket));
            connection.sendTCP(receiverInventoryPacket);

            PacketMessage packetMessage = PacketMessage.builder()
                    .packet(senderInventoryPacket)
                    .receivingPlayerId(sendPlayer.getId())
                    .build();
            rProducerService.send(packetMessage, "game.messenger.proposal chat.messenger.proposal", "GameServer");
        } else {
            message.setMessage("[Automatic response] I denied your proposal ＞﹏＜");
        }

        messageService.save(message);
        proposalService.remove(proposal.getId());

        S2CReceivedMessageNotificationPacket s2CReceivedMessageNotificationPacket = new S2CReceivedMessageNotificationPacket(message, receivePlayer.getName());
        PacketMessage packetMessage = PacketMessage.builder()
                .packet(s2CReceivedMessageNotificationPacket)
                .receivingPlayerId(sendPlayer.getId())
                .build();
        rProducerService.send(packetMessage, "game.messenger.proposal chat.messenger.proposal", "GameServer");
    }
}
