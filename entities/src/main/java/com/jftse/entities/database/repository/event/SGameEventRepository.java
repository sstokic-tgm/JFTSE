package com.jftse.entities.database.repository.event;

import com.jftse.entities.database.model.event.SGameEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SGameEventRepository extends JpaRepository<SGameEvent, Long> {
    Optional<SGameEvent> findByName(String name);
    List<SGameEvent> findAllByType(String type);
    List<SGameEvent> findAllByEnabledTrue();
}
