package com.ft.emulator.server.game.core.service;

import com.ft.emulator.server.database.model.battle.Guardian;
import com.ft.emulator.server.database.repository.battle.GuardianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class GuardianService {
    private final GuardianRepository guardianRepository;

    public Guardian findGuardianById(Long id) {
        Optional<Guardian> guardian = guardianRepository.findById(id);
        return guardian.orElse(null);
    }
}
