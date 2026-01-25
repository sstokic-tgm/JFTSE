package com.jftse.entities.database.repository.messenger;

import com.jftse.entities.database.model.messenger.Proposal;
import com.jftse.entities.database.model.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    Optional<Proposal> findById(Long id);
    List<Proposal> findBySender(Player sender);
    List<Proposal> findByReceiver(Player receiver);

    @Query(value = "SELECT p FROM Proposal p JOIN FETCH p.receiver WHERE p.sender.id = :playerId")
    List<Proposal> findWithPlayerBySender(Long playerId);

    @Query(value = "SELECT p FROM Proposal p JOIN FETCH p.sender WHERE p.receiver.id = :playerId")
    List<Proposal> findWithPlayerByReceiver(Long playerId);

    long deleteBySender(Player sender);
    long deleteByReceiver(Player receiver);
}