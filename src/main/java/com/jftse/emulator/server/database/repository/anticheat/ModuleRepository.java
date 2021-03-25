package com.jftse.emulator.server.database.repository.anticheat;

import com.jftse.emulator.server.database.model.anticheat.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    Optional<Module> findModuleByName(String name);
}