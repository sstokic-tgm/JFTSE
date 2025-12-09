package com.jftse.emulator.server.core.handler.messenger;

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

    private final RProducerService rProducerService;

    public ProposalAnswerRequestHandler() {
        proposalService = ServiceManager.getInstance().getProposalService();
        friendService = ServiceManager.getInstance().getFriendService();
        messageService = ServiceManager.getInstance().getMessageService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        socialService = ServiceManager.getInstance().getSocialService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGAcceptProposal packet) {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        Proposal proposal = proposalService.findById((long) packet.getProposalId());
        if (proposal == null) return;

        List<Friend> senderFriend = friendService.findByPlayer(proposal.getSender());
        if (senderFriend.stream().anyMatch(x -> x.getEFriendshipState().equals(EFriendshipState.Relationship))) {
            if (packet.getAccept()) {
                Message message = new Message();
                message.setSeen(false);
                message.setSender(proposal.getSender());
                message.setReceiver(proposal.getReceiver());
                message.setMessage("[Automatic response] I'm sorry but I'm already in a relationship");
                messageService.save(message);

                S2CReceivedMessageNotificationPacket s2CReceivedMessageNotificationPacket = new S2CReceivedMessageNotificationPacket(message);
                connection.sendTCP(s2CReceivedMessageNotificationPacket);
            }

            proposalService.remove(proposal.getId());
            return;
        }

        Message message = new Message();
        message.setSeen(false);
        message.setSender(proposal.getReceiver());
        message.setReceiver(proposal.getSender());
        if (packet.getAccept()) {
            List<Friend> receiverFriend = friendService.findByPlayer(proposal.getReceiver());
            if (receiverFriend.stream().anyMatch(x -> x.getEFriendshipState().equals(EFriendshipState.Relationship))) {
                proposalService.remove(proposal.getId());
                return;
            }

            message.setMessage("[Automatic response] I accepted your proposal <3");

            Friend friendOfSender = friendService.findByPlayerIdAndFriendId(
                    proposal.getSender().getId(),
                    proposal.getReceiver().getId());
            if (friendOfSender == null) {
                friendOfSender = new Friend();
                friendOfSender.setPlayer(proposal.getSender());
                friendOfSender.setFriend(proposal.getReceiver());
            }

            friendOfSender.setEFriendshipState(EFriendshipState.Relationship);

            Friend friendOfReceiver = friendService.findByPlayerIdAndFriendId(
                    proposal.getReceiver().getId(),
                    proposal.getSender().getId());
            if (friendOfReceiver == null) {
                friendOfReceiver = new Friend();
                friendOfReceiver.setPlayer(proposal.getReceiver());
                friendOfReceiver.setFriend(proposal.getSender());
            }

            friendOfReceiver.setEFriendshipState(EFriendshipState.Relationship);

            friendService.save(friendOfSender);
            friendService.save(friendOfReceiver);

            Friend myRelation = socialService.getRelationship(ftClient.getPlayer());
            if (myRelation != null) {
                S2CRelationshipAnswerPacket s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(myRelation);
                connection.sendTCP(s2CRelationshipAnswerPacket);

                RefreshFriendRelationMessage refreshFriendRelationMessage = RefreshFriendRelationMessage.builder()
                        .playerId(ftClient.getPlayer().getId())
                        .build();
                rProducerService.send(refreshFriendRelationMessage, "game.messenger.relationship chat.messenger.relationship", "ChatServer");
            }

            PlayerPocket senderPocket = new PlayerPocket();
            senderPocket.setPocket(proposal.getSender().getPocket());
            senderPocket.setCategory(EItemCategory.SPECIAL.getName());
            senderPocket.setUseType(EItemUseType.INSTANT.getName());
            senderPocket.setItemCount(1);
            senderPocket.setItemIndex(26);
            senderPocket = playerPocketService.save(senderPocket);

            PlayerPocket receiverPocket = new PlayerPocket();
            receiverPocket.setPocket(proposal.getReceiver().getPocket());
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
                    .receivingPlayerId(proposal.getSender().getId())
                    .build();
            rProducerService.send(packetMessage, "game.messenger.proposal chat.messenger.proposal", "ChatServer");
        } else {
            message.setMessage("[Automatic response] I denied your proposal ＞﹏＜");
        }

        messageService.save(message);
        proposalService.remove(proposal.getId());

        S2CReceivedMessageNotificationPacket s2CReceivedMessageNotificationPacket = new S2CReceivedMessageNotificationPacket(message);
        PacketMessage packetMessage = PacketMessage.builder()
                .packet(s2CReceivedMessageNotificationPacket)
                .receivingPlayerId(proposal.getSender().getId())
                .build();
        rProducerService.send(packetMessage, "game.messenger.proposal chat.messenger.proposal", "ChatServer");
    }
}
