package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.battle.BossGuardian;
import com.jftse.entities.database.repository.battle.BossGuardianRepository;
import com.jftse.server.core.service.BossGuardianService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class BossGuardianServiceImpl implements BossGuardianService {
    private final BossGuardianRepository bossGuardianRepository;

    @Override
    public BossGuardian findBossGuardianById(Long id) {
        Optional<BossGuardian> bossGuardian = bossGuardianRepository.findById(id);
        return bossGuardian.orElse(null);
    }
}
