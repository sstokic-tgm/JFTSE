package com.jftse.server.core.service.impl;

import com.jftse.emulator.common.utilities.StringTokenizer;
import com.jftse.entities.database.model.battle.SkillDropRate;
import com.jftse.entities.database.repository.battle.SkillDropRateRepository;
import com.jftse.server.core.service.SkillDropRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class SkillDropRateServiceImpl implements SkillDropRateService {
    private final SkillDropRateRepository skillDropRateRepository;

    private List<SkillDropRate> skillDropRates;
    private final Map<Long, List<Integer>> dropRateCache = new HashMap<>();

    @PostConstruct
    public void init() {
        skillDropRates = skillDropRateRepository.findAll();

        for (SkillDropRate rate : skillDropRates) {
            List<Integer> weights = new StringTokenizer(rate.getDropRates(), ",").get().stream()
                    .limit(16)
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();
            dropRateCache.put(rate.getId(), weights);
        }
    }

    @Override
    public SkillDropRate findSkillDropRateByPlayerLevel(int playerLevel) {
        return skillDropRates.stream()
                .filter(x -> x.getFromLevel() <= playerLevel && x.getToLevel() >= playerLevel)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Integer> getDropRatesForSkillDropRate(SkillDropRate skillDropRate) {
        return dropRateCache.getOrDefault(skillDropRate.getId(), List.of());
    }
}
