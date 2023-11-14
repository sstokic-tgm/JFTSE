package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.scenario.MScenarios;
import com.jftse.entities.database.repository.scenario.ScenariosRepository;
import com.jftse.server.core.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
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
}
