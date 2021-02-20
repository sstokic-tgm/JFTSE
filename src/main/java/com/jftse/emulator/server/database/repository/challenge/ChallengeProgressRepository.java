package com.jftse.emulator.server.database.repository.challenge;

import com.jftse.emulator.server.database.model.challenge.Challenge;
import com.jftse.emulator.server.database.model.challenge.ChallengeProgress;
import com.jftse.emulator.server.database.model.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChallengeProgressRepository extends JpaRepository<ChallengeProgress, Long> {
    @Query(value = "FROM ChallengeProgress cp LEFT JOIN FETCH cp.player player LEFT JOIN FETCH cp.challenge challenge WHERE player_id = :playerId")
    List<ChallengeProgress> findAllByPlayerIdFetched(@Param("playerId") Long playerId);

    Optional<ChallengeProgress> findByPlayerAndChallenge(Player player, Challenge challenge);
}
