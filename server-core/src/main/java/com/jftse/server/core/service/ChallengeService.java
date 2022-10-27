package com.jftse.server.core.service;

import com.jftse.entities.database.model.challenge.Challenge;
import com.jftse.entities.database.model.challenge.ChallengeProgress;
import com.jftse.server.core.net.Client;
import com.jftse.server.core.net.Connection;

import java.util.List;

public interface ChallengeService {
    List<ChallengeProgress> findAllByPlayerIdFetched(Long playerId);

    Challenge findChallengeByChallengeIndex(Integer challengeIndex);

    void finishGame(Connection<? extends Client<?>> connection, boolean win);
}
