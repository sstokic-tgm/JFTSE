package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.core.packets.messenger.C2SProposalAnswerRequestPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CReceivedMessageNotificationPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CRelationshipAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.messenger.Message;
import com.jftse.entities.database.model.messenger.Proposal;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.*;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SProposalAnswerRequest)
public class ProposalAnswerRequestHandler extends AbstractPacketHandler {
    private C2SProposalAnswerRequestPacket c2SProposalAnswerRequestPacket;

    private final ProposalService proposalService;
    private final FriendService friendService;
    private final MessageService messageService;
    private final PlayerPocketService playerPocketService;
    private final SocialService socialService;

    public ProposalAnswerRequestHandler() {
        proposalService = ServiceManager.getInstance().getProposalService();
        friendService = ServiceManager.getInstance().getFriendService();
        messageService = ServiceManager.getInstance().getMessageService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        socialService = ServiceManager.getInstance().getSocialService();
    }

    @Override
    public boolean process(Packet packet) {
        c2SProposalAnswerRequestPacket = new C2SProposalAnswerRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        Proposal proposal = proposalService.findById(c2SProposalAnswerRequestPacket.getProposalId().longValue());
        if (proposal == null) return;

        List<Friend> senderFriend = friendService.findByPlayer(proposal.getSender());
        if (senderFriend.stream().anyMatch(x -> x.getEFriendshipState().equals(EFriendshipState.Relationship))) {
            if (c2SProposalAnswerRequestPacket.getAccepted()) {
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
        if (c2SProposalAnswerRequestPacket.getAccepted()) {
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

                FTClient friendRelationClient = GameManager.getInstance().getClients().stream()
                        .filter(x -> x.getPlayer() != null && x.getPlayer().getId().equals(myRelation.getFriend().getId()))
                        .findFirst()
                        .orElse(null);
                Friend friendRelation = socialService.getRelationship(myRelation.getFriend());
                if (friendRelationClient != null && friendRelation != null) {
                    s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(friendRelation);
                    friendRelationClient.getConnection().sendTCP(s2CRelationshipAnswerPacket);
                }
            }

            PlayerPocket senderPocket = new PlayerPocket();
            senderPocket.setPocket(proposal.getSender().getPocket());
            senderPocket.setCategory(EItemCategory.SPECIAL.getName());
            senderPocket.setUseType(EItemUseType.INSTANT.getName());
            senderPocket.setItemCount(1);
            senderPocket.setItemIndex(26);
            playerPocketService.save(senderPocket);

            PlayerPocket receiverPocket = new PlayerPocket();
            receiverPocket.setPocket(proposal.getReceiver().getPocket());
            receiverPocket.setCategory(EItemCategory.SPECIAL.getName());
            receiverPocket.setUseType(EItemUseType.INSTANT.getName());
            receiverPocket.setItemCount(1);
            receiverPocket.setItemIndex(26);
            playerPocketService.save(receiverPocket);

            List<PlayerPocket> receiverItems = playerPocketService.getPlayerPocketItems(proposal.getReceiver().getPocket());
            S2CInventoryDataPacket receiverInventoryPacket = new S2CInventoryDataPacket(receiverItems);
            connection.sendTCP(receiverInventoryPacket);

        } else {
            message.setMessage("[Automatic response] I denied your proposal ＞﹏＜");
        }

        messageService.save(message);
        proposalService.remove(proposal.getId());

        FTClient senderClient = GameManager.getInstance().getClients().stream()
                .filter(x -> x.getPlayer() != null && x.getPlayer().getId().equals(proposal.getSender().getId()))
                .findFirst()
                .orElse(null);
        if (senderClient != null) {
            S2CReceivedMessageNotificationPacket s2CReceivedMessageNotificationPacket = new S2CReceivedMessageNotificationPacket(message);
            senderClient.getConnection().sendTCP(s2CReceivedMessageNotificationPacket);

            List<PlayerPocket> senderItems = playerPocketService.getPlayerPocketItems(proposal.getSender().getPocket());
            S2CInventoryDataPacket senderInventoryPacket = new S2CInventoryDataPacket(senderItems);
            senderClient.getConnection().sendTCP(senderInventoryPacket);
        }
    }
}
