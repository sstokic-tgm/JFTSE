package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.battle.Skill;
import com.jftse.entities.database.repository.battle.SkillRepository;
import com.jftse.server.core.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {
    private final SkillRepository skillRepository;

    private Skill[] skills;

    @PostConstruct
    public void init() {
        skills = skillRepository.findAll().toArray(Skill[]::new);
    }

    @Override
    public Skill findSkillById(Long id) {
        return findSkillByIndex(id.intValue() - 1);
    }

    @Override
    public Skill findSkillByIndex(int index) {
        if (index < 0 || index >= skills.length) {
            return null;
        }
        return skills[index];
    }
}
