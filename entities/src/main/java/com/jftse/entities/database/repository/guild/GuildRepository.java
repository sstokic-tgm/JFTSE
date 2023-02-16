package com.jftse.entities.database.repository.guild;

import com.jftse.entities.database.model.guild.Guild;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuildRepository extends JpaRepository<Guild, Long> {
    Optional<Guild> findById(Long id);
    List<Guild> findAllByName(String name);
    List<Guild> findAllByNameContainingIgnoreCase(String name);
}
