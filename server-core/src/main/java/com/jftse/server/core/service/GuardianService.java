package com.jftse.server.core.service;

import com.jftse.entities.database.model.battle.Guardian;

import java.util.List;

public interface GuardianService {
    Guardian findGuardianById(Long id);

    List<Guardian> findGuardiansByIds(List<Integer> ids);
}
