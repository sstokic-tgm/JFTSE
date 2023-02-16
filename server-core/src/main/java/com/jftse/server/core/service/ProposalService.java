package com.jftse.server.core.service;

import com.jftse.entities.database.model.messenger.Proposal;
import com.jftse.entities.database.model.player.Player;

import java.util.List;

public interface ProposalService {
    Proposal save(Proposal proposal);

    void remove(Long proposalId);

    Proposal findById(Long id);

    List<Proposal> findBySender(Player sender);

    List<Proposal> findByReceiver(Player receiver);

    long deleteBySender(Player sender);

    long deleteByReceiver(Player receiver);
}
