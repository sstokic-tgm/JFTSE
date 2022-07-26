package com.jftse.entities.database.repository.config;

import com.jftse.entities.database.model.config.Config;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigRepository extends JpaRepository<Config, Long> {
    Optional<Config> findConfigByName(String name);
}
