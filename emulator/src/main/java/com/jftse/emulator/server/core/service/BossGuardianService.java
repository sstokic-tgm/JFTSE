package com.jftse.emulator.server.core.service;

import com.jftse.entities.database.model.battle.BossGuardian;
import com.jftse.entities.database.repository.battle.BossGuardianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class BossGuardianService {
    private final BossGuardianRepository bossGuardianRepository;

    public BossGuardian findBossGuardianById(Long id) {
        Optional<BossGuardian> bossGuardian = bossGuardianRepository.findById(id);
        return bossGuardian.orElse(null);
    }
}
