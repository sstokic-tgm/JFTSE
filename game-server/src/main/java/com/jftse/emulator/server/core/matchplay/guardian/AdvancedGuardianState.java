package com.jftse.emulator.server.core.matchplay.guardian;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.battle.GuardianBase;
import com.jftse.entities.database.model.battle.Skill;
import com.jftse.entities.database.model.battle.Skill2Guardians;
import com.jftse.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.server.core.matchplay.battle.SkillDrop;
import com.jftse.server.core.service.GuardianSkillsService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Getter
@Setter
@Log4j2
public class AdvancedGuardianState extends GuardianBattleState {
    private List<Skill2Guardians> skills;

    private Long mapId;
    private Long scenario;

    private final Random random = new Random();

    public AdvancedGuardianState(Long mapId, Long scenarioId, GuardianBase guardian, short position, int hp, int str, int sta, int dex, int will, int exp, int gold, int rewardRankingPoint) {
        super(guardian, position, hp, str, sta, dex, will, exp, gold, rewardRankingPoint);

        this.skills = new ArrayList<>();

        this.mapId = mapId;
        this.scenario = scenarioId;
    }

    @Override
    public AdvancedGuardianState self() {
        return this;
    }

    @Override
    public void loadSkills() {
        GuardianSkillsService guardianSkillsService = ServiceManager.getInstance().getGuardianSkillsService();

        List<Skill2Guardians> skill2Guardians = new ArrayList<>();
        List<Skill2Guardians> btSkills = guardianSkillsService.findAllByBtItemId(getBtItemId());
        if (btSkills != null) {
            skill2Guardians.addAll(btSkills);
        }

        if (isBoss()) {
            List<Skill2Guardians> bossSkills = guardianSkillsService.getSkillsByMapAndBossGuardianAndScenario(getMapId(), (long) getId(), getScenario());
            if (bossSkills != null) {
                mergeAndUpdateSkills(skill2Guardians, bossSkills);
            }
        } else {
            List<Skill2Guardians> guardianSkills = guardianSkillsService.getSkillsByMapAndGuardianAndScenario(getMapId(), (long) getId(), getScenario());
            if (guardianSkills != null) {
                mergeAndUpdateSkills(skill2Guardians, guardianSkills);
            }
        }

        if (skill2Guardians.isEmpty()) {
            log.error("No skills found for guardian: {}, btItemId: {}", getId(), getBtItemId());
        } else {
            this.skills.addAll(skill2Guardians);
        }
    }

    private void mergeAndUpdateSkills(List<Skill2Guardians> toUpdate, List<Skill2Guardians> toMerge) {
        for (Skill2Guardians skill : toMerge) {
            Optional<Skill2Guardians> existingSkill = toUpdate.stream()
                    .filter(s2g -> s2g.getSkill().getId().equals(skill.getSkill().getId()))
                    .findFirst();

            if (existingSkill.isEmpty()) {
                toUpdate.add(skill);
            } else {
                toUpdate.remove(existingSkill.get());
                toUpdate.add(skill);
            }
        }
    }

    @Override
    public Skill getRandomGuardianSkillBasedOnProbability() {
        final double totalChance = skills.stream()
                .map(Skill2Guardians::getChance)
                .reduce(0.0, Double::sum);

        final List<SkillDrop> skillDrops = new ArrayList<>();
        double currentChance = 0.0;
        for (int i = 0; i < skills.size(); i++) {
            Skill2Guardians skill2Guardian = skills.get(i);
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

        final Skill2Guardians skill2Guardian = skills.get(skillDrop.getId());
        return skill2Guardian.getSkill();
    }
}
