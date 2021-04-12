package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.server.database.model.battle.BossGuardian;
import com.jftse.emulator.server.database.model.battle.Guardian;
import com.jftse.emulator.server.database.repository.battle.BossGuardianRepository;
import com.jftse.emulator.server.database.repository.battle.GuardianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
