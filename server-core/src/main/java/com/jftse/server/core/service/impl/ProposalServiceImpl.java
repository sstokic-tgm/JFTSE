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
public class ProposalServiceImpl implements ProposalService {
    private final ProposalRepository proposalRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Proposal save(Proposal proposal) {
        return proposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public void remove(Long proposalId) {
        proposalRepository.deleteById(proposalId);
    }

    @Override
    @Transactional(readOnly = true)
    public Proposal findById(Long id) {
        return proposalRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proposal> findBySender(Player sender) {
        return proposalRepository.findBySender(sender);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proposal> findWithPlayerBySender(Long playerId) {
        return proposalRepository.findWithPlayerBySender(playerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proposal> findByReceiver(Player receiver) {
        return proposalRepository.findByReceiver(receiver);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proposal> findWithPlayerByReceiver(Long playerId) {
        return proposalRepository.findWithPlayerByReceiver(playerId);
    }

    @Override
    @Transactional
    public long deleteBySender(Player sender) {
        return proposalRepository.deleteBySender(sender);
    }

    @Override
    @Transactional
    public long deleteByReceiver(Player receiver) {
        return proposalRepository.deleteByReceiver(receiver);
    }
}
