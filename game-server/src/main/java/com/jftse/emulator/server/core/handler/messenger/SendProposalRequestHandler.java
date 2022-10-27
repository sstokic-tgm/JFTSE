package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.core.packets.messenger.C2SSendProposalRequestPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CProposalDeliveredAnswerPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CProposalListPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CReceivedProposalNotificationPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.messenger.Proposal;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.FriendService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.ProposalService;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SSendProposalRequest)
public class SendProposalRequestHandler extends AbstractPacketHandler {
    private C2SSendProposalRequestPacket c2SSendProposalRequestPacket;

    private final PlayerPocketService playerPocketService;
    private final ProposalService proposalService;
    private final PlayerService playerService;
    private final FriendService friendService;

    public SendProposalRequestHandler() {
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        proposalService = ServiceManager.getInstance().getProposalService();
        playerService = ServiceManager.getInstance().getPlayerService();
        friendService = ServiceManager.getInstance().getFriendService();
    }

    @Override
    public boolean process(Packet packet) {
        c2SSendProposalRequestPacket = new C2SSendProposalRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        PlayerPocket item = playerPocketService.findById(c2SSendProposalRequestPacket.getPlayerPocketId().longValue());

        // 0 = MSG_PROPOSE_SUCCESS
        //-1 = MSG_NO_CHARACTER_AT_CHARACTER_LIST
        //-3 = MSG_PROPOSE_ACCEPT_FAILED_ALREADY_COUPLE
        //-4 = MSG_PROPOSE_FAILED_ALREADY_PROPOSING
        //-6 = MSG_YOU_CAN_NOT_PROPOSE_FOR_SAME_ACCOUNT
        //-7 = MSG_NO_HAVE_PROPOSE_ITEM
        //-9 = MSG_YOU_CAN_NOT_PROPOSE_FOR_SAME_SEX
        boolean isValidProposalItem = item != null && (
                item.getItemIndex().equals(23) ||
                        item.getItemIndex().equals(24) ||
                        item.getItemIndex().equals(25));
        if (!isValidProposalItem) {
            S2CProposalDeliveredAnswerPacket proposalDeliveredAnswerPacket = new S2CProposalDeliveredAnswerPacket((byte) -7);
            connection.sendTCP(proposalDeliveredAnswerPacket);
            return;
        }

        Player sender = ftClient.getPlayer();
        Player receiver = playerService.findByName(c2SSendProposalRequestPacket.getReceiverName());

        List<Friend> senderFriend = friendService.findByPlayer(sender);
        if (senderFriend.stream().anyMatch(x -> x.getEFriendshipState().equals(EFriendshipState.Relationship))) {
            S2CProposalDeliveredAnswerPacket proposalDeliveredAnswerPacket = new S2CProposalDeliveredAnswerPacket((byte) -3);
            connection.sendTCP(proposalDeliveredAnswerPacket);
            return;
        }

        if (receiver != null) {
            List<Friend> receiverFriends = friendService.findByPlayer(receiver);
            if (receiverFriends.stream().anyMatch(x -> x.getEFriendshipState().equals(EFriendshipState.Relationship))) {
                S2CProposalDeliveredAnswerPacket proposalDeliveredAnswerPacket = new S2CProposalDeliveredAnswerPacket((byte) -3);
                connection.sendTCP(proposalDeliveredAnswerPacket);
                return;
            }

            Proposal proposal = new Proposal();
            proposal.setReceiver(receiver);
            proposal.setSender(sender);
            proposal.setMessage(c2SSendProposalRequestPacket.getMessage());
            proposal.setSeen(false);
            proposal.setCategory(item.getCategory());
            proposal.setItemIndex(item.getItemIndex());

            proposalService.save(proposal);
            int newItemCount = item.getItemCount() - 1;
            if (newItemCount < 1) {
                playerPocketService.remove(item.getId());
                S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket =
                        new S2CInventoryItemRemoveAnswerPacket(item.getId().intValue());
                connection.sendTCP(inventoryItemRemoveAnswerPacket);
            } else {
                item.setItemCount(newItemCount);
                playerPocketService.save(item);
                List<PlayerPocket> items = playerPocketService.getPlayerPocketItems(sender.getPocket());
                S2CInventoryDataPacket s2CInventoryDataPacket = new S2CInventoryDataPacket(items);
                connection.sendTCP(s2CInventoryDataPacket);
            }

            FTClient receiverClient = GameManager.getInstance().getClients().stream()
                    .filter(x -> x.getPlayer() != null && x.getPlayer().getId().equals(receiver.getId()))
                    .findFirst()
                    .orElse(null);
            if (receiverClient != null) {
                S2CReceivedProposalNotificationPacket s2CReceivedProposalNotificationPacket = new S2CReceivedProposalNotificationPacket(proposal);
                receiverClient.getConnection().sendTCP(s2CReceivedProposalNotificationPacket);
            }

            List<Proposal> sentProposals = proposalService.findBySender(sender);
            S2CProposalListPacket s2CSentProposalListPacket = new S2CProposalListPacket((byte) 1, sentProposals);
            connection.sendTCP(s2CSentProposalListPacket);

            S2CProposalDeliveredAnswerPacket s2CProposalDeliveredAnswerPacket = new S2CProposalDeliveredAnswerPacket((byte) 0);
            connection.sendTCP(s2CProposalDeliveredAnswerPacket);
        }
    }
}
