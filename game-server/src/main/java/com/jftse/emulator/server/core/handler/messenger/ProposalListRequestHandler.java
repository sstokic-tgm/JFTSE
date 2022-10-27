package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.packets.messenger.C2SProposalListRequestPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CProposalListPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.Proposal;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.ProposalService;

import java.util.ArrayList;
import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SProposalListRequest)
public class ProposalListRequestHandler extends AbstractPacketHandler {
    private C2SProposalListRequestPacket proposalListRequestPacket;

    private final ProposalService proposalService;

    public ProposalListRequestHandler() {
        proposalService = ServiceManager.getInstance().getProposalService();
    }

    @Override
    public boolean process(Packet packet) {
        proposalListRequestPacket = new C2SProposalListRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
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
