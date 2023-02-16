package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.messenger.Proposal;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.messenger.ProposalRepository;
import com.jftse.server.core.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ProposalServiceImpl implements ProposalService {
    private final ProposalRepository proposalRepository;

    @Override
    public Proposal save(Proposal proposal) {
        return proposalRepository.save(proposal);
    }

    @Override
    public void remove(Long proposalId) {
        proposalRepository.deleteById(proposalId);
    }

    @Override
    public Proposal findById(Long id) { return proposalRepository.findById(id).orElse(null); }

    @Override
    public List<Proposal> findBySender(Player sender) { return proposalRepository.findBySender(sender); }

    @Override
    public List<Proposal> findByReceiver(Player receiver) { return proposalRepository.findByReceiver(receiver); }

    @Override
    public long deleteBySender(Player sender) {
        return proposalRepository.deleteBySender(sender);
    }

    @Override
    public long deleteByReceiver(Player receiver) {
        return proposalRepository.deleteByReceiver(receiver);
    }
}
