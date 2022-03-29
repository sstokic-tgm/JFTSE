package com.jftse.emulator.server.database.repository.messenger;

import com.jftse.emulator.server.database.model.messenger.Proposal;
import com.jftse.emulator.server.database.model.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    Optional<Proposal> findById(Long id);
    List<Proposal> findBySender(Player sender);
    List<Proposal> findByReceiver(Player receiver);
    long deleteBySender(Player sender);
    long deleteByReceiver(Player receiver);
}