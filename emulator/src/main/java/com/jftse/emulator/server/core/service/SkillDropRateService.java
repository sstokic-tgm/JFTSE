package com.jftse.emulator.server.core.service;

import com.jftse.entities.database.model.battle.SkillDropRate;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.battle.SkillDropRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class SkillDropRateService {
    private final SkillDropRateRepository skillDropRateRepository;

    public SkillDropRate findSkillDropRateByPlayer(Player player) {
        Byte playerLevel = player.getLevel();
        List<SkillDropRate> skillDropRates = skillDropRateRepository.findAll();
        return skillDropRates.stream()
                .filter(x -> x.getFromLevel() <= playerLevel && x.getToLevel() >= playerLevel)
                .findFirst()
                .orElse(null);
    }
}
