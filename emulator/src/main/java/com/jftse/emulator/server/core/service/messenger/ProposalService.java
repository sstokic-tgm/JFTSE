package com.jftse.emulator.server.core.service.messenger;

import com.jftse.entities.database.model.messenger.Proposal;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.messenger.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ProposalService {
    private final ProposalRepository proposalRepository;

    public Proposal save(Proposal proposal) {
        return proposalRepository.save(proposal);
    }

    public void remove(Long proposalId) {
        proposalRepository.deleteById(proposalId);
    }

    public Proposal findById(Long id) { return proposalRepository.findById(id).orElse(null); }

    public List<Proposal> findBySender(Player sender) { return proposalRepository.findBySender(sender); }

    public List<Proposal> findByReceiver(Player receiver) { return proposalRepository.findByReceiver(receiver); }

    public long deleteBySender(Player sender) {
        return proposalRepository.deleteBySender(sender);
    }

    public long deleteByReceiver(Player receiver) {
        return proposalRepository.deleteByReceiver(receiver);
    }
}
