package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryItemCountPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CProposalListPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CReceivedProposalNotificationPacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.model.log.GameLogType;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.messenger.Proposal;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import com.jftse.server.core.shared.packets.messenger.CMSGSendProposal;
import com.jftse.server.core.shared.packets.messenger.SMSGSendProposal;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;

import java.util.List;

@PacketId(CMSGSendProposal.PACKET_ID)
public class SendProposalRequestHandler implements PacketHandler<FTConnection, CMSGSendProposal> {
    private final PlayerPocketService playerPocketService;
    private final ProposalService proposalService;
    private final PlayerService playerService;
    private final FriendService friendService;

    private final GameLogService gameLogService;

    private final RProducerService rProducerService;

    public SendProposalRequestHandler() {
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        proposalService = ServiceManager.getInstance().getProposalService();
        playerService = ServiceManager.getInstance().getPlayerService();
        friendService = ServiceManager.getInstance().getFriendService();
        gameLogService = ServiceManager.getInstance().getGameLogService();
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGSendProposal packet) {
        FTClient ftClient = connection.getClient();
        if (ftClient == null)
            return;

        PlayerPocket item = playerPocketService.findById((long) packet.getPlayerPocketId());

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
            SMSGSendProposal response = SMSGSendProposal.builder().status((byte) -7).build();
            connection.sendTCP(response);
            return;
        }

        Player sender = ftClient.getPlayer();
        if (sender == null)
            return;

        if (!sender.getPocket().getId().equals(item.getPocket().getId())) {
            SMSGSendProposal response = SMSGSendProposal.builder().status((byte) -2).build();
            connection.sendTCP(response);

            GameLog gameLog = new GameLog();
            gameLog.setGameLogType(GameLogType.BANABLE);
            gameLog.setContent("pockets are not equal! requested pocketId: " + item.getPocket().getId() + ", requested playerPocketId: " + item.getId() + ", requesting player pocketId: " + sender.getPocket().getId() + ", requesting playerId: " + sender.getId());
            gameLogService.save(gameLog);

            return;
        }

        Player receiver = playerService.findByName(packet.getReceiverName());

        List<Friend> senderFriend = friendService.findByPlayer(sender);
        if (senderFriend.stream().anyMatch(x -> x.getEFriendshipState().equals(EFriendshipState.Relationship))) {
            SMSGSendProposal response = SMSGSendProposal.builder().status((byte) -3).build();
            connection.sendTCP(response);
            return;
        }

        if (receiver != null) {
            List<Friend> receiverFriends = friendService.findByPlayer(receiver);
            if (receiverFriends.stream().anyMatch(x -> x.getEFriendshipState().equals(EFriendshipState.Relationship))) {
                SMSGSendProposal response = SMSGSendProposal.builder().status((byte) -3).build();
                connection.sendTCP(response);
                return;
            }

            Proposal proposal = new Proposal();
            proposal.setReceiver(receiver);
            proposal.setSender(sender);
            proposal.setMessage(packet.getMessage());
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
                S2CInventoryItemCountPacket inventoryItemCountPacket = new S2CInventoryItemCountPacket(item);
                connection.sendTCP(inventoryItemCountPacket);
            }

            S2CReceivedProposalNotificationPacket s2CReceivedProposalNotificationPacket = new S2CReceivedProposalNotificationPacket(proposal);

            PacketMessage packetMessage = PacketMessage.builder()
                    .receivingPlayerId(receiver.getId())
                    .packet(s2CReceivedProposalNotificationPacket)
                    .build();
            rProducerService.send(packetMessage, "game.messenger.proposal chat.messenger.proposal", sender.getName() + "(GameServer)");

            List<Proposal> sentProposals = proposalService.findBySender(sender);
            S2CProposalListPacket s2CSentProposalListPacket = new S2CProposalListPacket((byte) 1, sentProposals);
            connection.sendTCP(s2CSentProposalListPacket);

            SMSGSendProposal response = SMSGSendProposal.builder().status((byte) 0).build();
            connection.sendTCP(response);
        }
    }
}
