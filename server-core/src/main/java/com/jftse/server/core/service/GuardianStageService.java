package com.jftse.server.core.service;

import com.jftse.entities.database.model.battle.GuardianStage;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

public interface GuardianStageService {
    @PostConstruct
    void init() throws IOException;

    List<GuardianStage> getGuardianStages();
}
