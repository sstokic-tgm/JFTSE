package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.battle.SkillDropRate;
import com.jftse.entities.database.repository.battle.SkillDropRateRepository;
import com.jftse.server.core.service.SkillDropRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SkillDropRateServiceImpl implements SkillDropRateService {
    private final SkillDropRateRepository skillDropRateRepository;

    @Override
    public SkillDropRate findSkillDropRateByPlayerLevel(int playerLevel) {
        List<SkillDropRate> skillDropRates = skillDropRateRepository.findAll();
        return skillDropRates.stream()
                .filter(x -> x.getFromLevel() <= playerLevel && x.getToLevel() >= playerLevel)
                .findFirst()
                .orElse(null);
    }
}
