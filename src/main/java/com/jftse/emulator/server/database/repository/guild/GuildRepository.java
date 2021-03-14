package com.jftse.emulator.server.database.repository.guild;

import com.jftse.emulator.server.database.model.guild.Guild;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuildRepository extends JpaRepository<Guild, Long> {
    Optional<Guild> findById(Long id);
    List<Guild> findAllByName(String name);
}
