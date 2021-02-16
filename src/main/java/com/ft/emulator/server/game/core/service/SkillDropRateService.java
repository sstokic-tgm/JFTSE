package com.ft.emulator.server.game.core.service;

import com.ft.emulator.server.database.model.battle.Skill;
import com.ft.emulator.server.database.model.battle.SkillDropRate;
import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.database.repository.item.SkillDropRateRepository;
import com.ft.emulator.server.database.repository.item.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class SkillDropRateService {
    private final SkillDropRateRepository skillDropRateRepository;

    public SkillDropRate findSkillDropRateByPlayer(Player player) {
        Byte playerLevel = player.getLevel();
        List<SkillDropRate> skillDropRates = skillDropRateRepository.findAll();
        SkillDropRate skillDropRate = skillDropRates.stream()
                .filter(x -> x.getFromLevel() <= playerLevel && x.getToLevel() >= playerLevel)
                .findFirst()
                .orElse(null);
        return skillDropRate;
    }
}
