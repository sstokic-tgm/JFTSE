package com.jftse.emulator.server.database.repository.battle;

import com.jftse.emulator.server.database.model.battle.BossGuardian;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BossGuardianRepository extends JpaRepository<BossGuardian, Long> {
}