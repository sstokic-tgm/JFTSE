package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.battle.Skill;
import com.jftse.entities.database.repository.battle.SkillRepository;
import com.jftse.server.core.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {
    private final SkillRepository skillRepository;

    @Override
    public Skill findSkillById(Long id) {
        Optional<Skill> skill = skillRepository.findById(id);
        return skill.orElse(null);
    }
}
