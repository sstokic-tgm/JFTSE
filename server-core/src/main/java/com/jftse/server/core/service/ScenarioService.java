package com.jftse.server.core.service;

import com.jftse.entities.database.model.scenario.MScenarios;

import java.util.List;

public interface ScenarioService {
    MScenarios getScenarioById(Long scenarioId);
    MScenarios getDefaultScenarioByGameMode(MScenarios.GameMode gameMode);
    MScenarios getDefaultScenarioByMapAndGameMode(Long mapId, MScenarios.GameMode gameMode);
    List<MScenarios> getByMapAndIsDefault(Long mapId, Boolean isDefault);
}
