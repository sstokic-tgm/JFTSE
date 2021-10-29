package com.jftse.emulator.server.core.service.messaging;

import com.jftse.emulator.server.database.model.messaging.Proposal;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.repository.messaging.ProposalRepository;
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

    public Proposal findById(Long id) { return proposalRepository.findById(id).get(); }

    public List<Proposal> findBySender(Player sender) { return proposalRepository.findBySender(sender); }

    public List<Proposal> findByReceiver(Player receiver) { return proposalRepository.findByReceiver(receiver); }

    public long deleteBySender(Player sender) {
        return proposalRepository.deleteBySender(sender);
    }

    public long deleteByReceiver(Player receiver) {
        return proposalRepository.deleteByReceiver(receiver);
    }
}
