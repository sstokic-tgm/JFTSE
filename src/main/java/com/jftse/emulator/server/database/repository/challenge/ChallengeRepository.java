package com.jftse.emulator.server.database.repository.challenge;

import com.jftse.emulator.server.database.model.challenge.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    Optional<Challenge> findChallengeByChallengeIndex(Integer challengeIndex);
}
