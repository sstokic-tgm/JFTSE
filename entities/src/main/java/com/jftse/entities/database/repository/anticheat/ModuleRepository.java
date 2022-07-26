package com.jftse.entities.database.repository.anticheat;

import com.jftse.entities.database.model.anticheat.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    Optional<Module> findModuleByName(String name);
}