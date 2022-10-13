package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.battle.SkillDropRate;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.battle.SkillDropRateRepository;
import com.jftse.server.core.service.SkillDropRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class SkillDropRateServiceImpl implements SkillDropRateService {
    private final SkillDropRateRepository skillDropRateRepository;

    @Override
    public SkillDropRate findSkillDropRateByPlayer(Player player) {
        Byte playerLevel = player.getLevel();
        List<SkillDropRate> skillDropRates = skillDropRateRepository.findAll();
        return skillDropRates.stream()
                .filter(x -> x.getFromLevel() <= playerLevel && x.getToLevel() >= playerLevel)
                .findFirst()
                .orElse(null);
    }
}
