package com.jftse.server.core.service;

import com.jftse.entities.database.model.tutorial.Tutorial;
import com.jftse.entities.database.model.tutorial.TutorialProgress;
import com.jftse.server.core.net.Client;
import com.jftse.server.core.net.Connection;

import java.util.List;

public interface TutorialService {
    List<TutorialProgress> findAllByPlayerIdFetched(Long playerId);

    Tutorial findByTutorialIndex(Integer tutorialIndex);

    void finishGame(Connection<? extends Client<?>> connection);
}
