package com.jftse.emulator.server.core.handler.game.messenger;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.messenger.C2SProposalListRequestPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CProposalListPacket;
import com.jftse.emulator.server.core.service.messenger.ProposalService;
import com.jftse.entities.database.model.messenger.Proposal;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class ProposalListRequestHandler extends AbstractHandler {
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
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        byte listType = proposalListRequestPacket.getListType();

        Player player = connection.getClient().getPlayer();

        List<Proposal> proposalList = new ArrayList<>();
        switch (listType) {
            case 0 -> proposalList.addAll(proposalService.findByReceiver(player));
            case 1 -> proposalList.addAll(proposalService.findBySender(player));
        }

        S2CProposalListPacket s2CReceivedProposalListPacket = new S2CProposalListPacket(listType, proposalList);
        connection.sendTCP(s2CReceivedProposalListPacket);
    }
}
