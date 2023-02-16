package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.battle.Guardian;
import com.jftse.entities.database.repository.battle.GuardianRepository;
import com.jftse.server.core.service.GuardianService;
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
public class GuardianServiceImpl implements GuardianService {
    private final GuardianRepository guardianRepository;

    @Override
    public Guardian findGuardianById(Long id) {
        Optional<Guardian> guardian = guardianRepository.findById(id);
        return guardian.orElse(null);
    }

    @Override
    public List<Guardian> findGuardiansByIds(List<Integer> ids) {
        List<Guardian> guardians = ids.stream()
                .map(x -> guardianRepository.findById((long) x)
                .orElse(null)).collect(Collectors.toList());
        return guardians;
    }
}
