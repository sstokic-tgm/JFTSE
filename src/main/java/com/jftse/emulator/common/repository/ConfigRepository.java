package com.jftse.emulator.common.repository;

import com.jftse.emulator.common.model.config.Config;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigRepository extends JpaRepository<Config, Long> {
    Optional<Config> findConfigByName(String name);
}
