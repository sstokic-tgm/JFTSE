package com.jftse.emulator.server.core.handler.game.messenger;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.C2SSendProposalRequestPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CProposalDeliveredAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CProposalListPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CReceivedProposalNotificationPacket;
import com.jftse.emulator.server.core.service.PlayerPocketService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.core.service.messenger.FriendService;
import com.jftse.emulator.server.core.service.messenger.ProposalService;
import com.jftse.emulator.server.database.model.messenger.EFriendshipState;
import com.jftse.emulator.server.database.model.messenger.Friend;
import com.jftse.emulator.server.database.model.messenger.Proposal;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.List;

public class SendProposalRequestHandler extends AbstractHandler {
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
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null)
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

        Player sender = playerService.findById(connection.getClient().getActivePlayer().getId());
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
            proposal.setSender(connection.getClient().getActivePlayer());
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

            Client receiverClient = GameManager.getInstance().getClients().stream()
                    .filter(x -> x.getActivePlayer() != null && x.getActivePlayer().getId().equals(receiver.getId()))
                    .findFirst()
                    .orElse(null);
            if (receiverClient != null) {
                S2CReceivedProposalNotificationPacket s2CReceivedProposalNotificationPacket = new S2CReceivedProposalNotificationPacket(proposal);
                connection.getServer().sendToTcp(receiverClient.getConnection().getId(), s2CReceivedProposalNotificationPacket);
            }

            List<Proposal> sentProposals = proposalService.findBySender(sender);
            S2CProposalListPacket s2CSentProposalListPacket = new S2CProposalListPacket((byte) 1, sentProposals);
            connection.sendTCP(s2CSentProposalListPacket);

            S2CProposalDeliveredAnswerPacket s2CProposalDeliveredAnswerPacket = new S2CProposalDeliveredAnswerPacket((byte) 0);
            connection.sendTCP(s2CProposalDeliveredAnswerPacket);
        }
    }
}
