package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.battle.Guardian2Maps;
import com.jftse.entities.database.model.battle.Skill;
import com.jftse.entities.database.model.battle.Skill2Guardians;
import com.jftse.entities.database.model.map.SMaps;
import com.jftse.entities.database.model.scenario.MScenarios;
import com.jftse.entities.database.repository.battle.Guardian2MapsRepository;
import com.jftse.entities.database.repository.battle.Skill2GuardiansRepository;
import com.jftse.entities.database.repository.map.MapsRepository;
import com.jftse.server.core.matchplay.battle.SkillDrop;
import com.jftse.server.core.service.GuardianSkillsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class GuardianSkillsServiceImpl implements GuardianSkillsService {
    private final Random random = new Random();

    private final Skill2GuardiansRepository skill2GuardiansRepository;
    private final Guardian2MapsRepository guardian2MapsRepository;

    @Override
    public Skill getRandomGuardianSkillBasedOnProbability(int btItemId, int guardianId, MScenarios scenario, SMaps map) {
        final List<Skill2Guardians> skill2Guardians = skill2GuardiansRepository.findAllByBtItemId(btItemId);
        assert !skill2Guardians.isEmpty() : "Skill2Guardians with btItemId " + btItemId + " not found";

        List<Guardian2Maps> guardians = guardian2MapsRepository.findAllByMapAndGuardianAndScenario(map.getId(), (long) guardianId, scenario.getId());
        List<Guardian2Maps> bossGuardians = guardian2MapsRepository.findAllByMapAndBossGuardianAndScenario(map.getId(), (long) guardianId, scenario.getId());

        addGuardianSkillsToSkills(skill2Guardians, guardians);
        addGuardianSkillsToSkills(skill2Guardians, bossGuardians);

        final double totalChance = skill2Guardians.stream()
                .map(Skill2Guardians::getChance)
                .reduce(0.0, Double::sum);

        final List<SkillDrop> skillDrops = new ArrayList<>();
        double currentChance = 0.0;
        for (int i = 0; i < skill2Guardians.size(); i++) {
            Skill2Guardians skill2Guardian = skill2Guardians.get(i);
            if (skill2Guardian.getChance() != 0.0) {
                skillDrops.add(new SkillDrop(i, currentChance, currentChance + skill2Guardian.getChance()));
                currentChance = currentChance + skill2Guardian.getChance();
            }
        }

        final double randomChance = random.nextDouble(totalChance);
        final SkillDrop skillDrop = skillDrops.stream()
                .filter(sp -> sp.getFrom() <= randomChance && sp.getTo() >= randomChance)
                .findFirst()
                .orElse(null);
        assert skillDrop != null : "SkillDrop not found";

        final Skill2Guardians skill2Guardian = skill2Guardians.get(skillDrop.getId());
        return skill2Guardian.getSkill();
    }

    private void addGuardianSkillsToSkills(List<Skill2Guardians> skill2Guardians, List<Guardian2Maps> guardians) {
        for (Guardian2Maps guard : guardians) {
            List<Skill2Guardians> guardianSkillList = skill2GuardiansRepository.findAllByGuardian(guard);
            for (Skill2Guardians skill : guardianSkillList) {
                final boolean existing = skill2Guardians.stream()
                        .anyMatch(s2g -> s2g.getSkill().getId().equals(skill.getSkill().getId()));
                if (!existing) {
                    skill2Guardians.add(skill);
                }
            }
        }
    }
}
