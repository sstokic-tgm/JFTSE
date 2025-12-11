package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.messenger.S2CProposalListPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.messenger.Proposal;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.ProposalService;
import com.jftse.server.core.shared.packets.messenger.CMSGProposalList;

import java.util.ArrayList;
import java.util.List;

@PacketId(CMSGProposalList.PACKET_ID)
public class ProposalListRequestHandler implements PacketHandler<FTConnection, CMSGProposalList> {
    private final ProposalService proposalService;

    public ProposalListRequestHandler() {
        proposalService = ServiceManager.getInstance().getProposalService();
    }

    @Override
    public void handle(FTConnection connection, CMSGProposalList proposalListRequestPacket) {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        byte listType = proposalListRequestPacket.getListType();

        Player player = ftClient.getPlayer();

        List<Proposal> proposalList = new ArrayList<>();
        switch (listType) {
            case 0 -> proposalList.addAll(proposalService.findByReceiver(player));
            case 1 -> proposalList.addAll(proposalService.findBySender(player));
        }

        S2CProposalListPacket s2CReceivedProposalListPacket = new S2CProposalListPacket(listType, proposalList);
        connection.sendTCP(s2CReceivedProposalListPacket);
    }
}
