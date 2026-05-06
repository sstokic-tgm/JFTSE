package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.scenario.MScenarios;
import com.jftse.entities.database.repository.scenario.ScenariosRepository;
import com.jftse.server.core.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScenarioServiceImpl implements ScenarioService {
    private final ScenariosRepository scenariosRepository;

    @Override
    public MScenarios getScenarioById(Long scenarioId) {
        return scenariosRepository.findByIdAndIsDefault(scenarioId, true).orElse(null);
    }

    @Override
    public MScenarios getDefaultScenarioByGameMode(MScenarios.GameMode gameMode) {
        return scenariosRepository.findAllByIsDefaultAndGameMode(true, gameMode).stream().findFirst().orElse(null);
    }

    @Override
    public MScenarios getDefaultScenarioByMapAndGameMode(Long mapId, MScenarios.GameMode gameMode) {
        return scenariosRepository.findAllByMapsAndGameModeAndIsDefault(Set.of(mapId), gameMode, true).stream().findFirst().orElse(null);
    }

    @Override
    public List<MScenarios> getByMapAndIsDefault(Long mapId, Boolean isDefault) {
        return scenariosRepository.findAllByMapAndIsDefault(mapId, isDefault);
    }
}
