package com.jftse.server.core.service;

import com.jftse.entities.database.model.event.SGameEvent;

import java.util.List;
import java.util.Optional;

public interface GameEventService {
    Optional<SGameEvent> findById(Long id);
    Optional<SGameEvent> findByName(String name);
    List<SGameEvent> findAll();
    List<SGameEvent> findAllEnabled();
    List<SGameEvent> findAllByType(String type);
    SGameEvent save(SGameEvent gameEvent);
    void remove(SGameEvent gameEvent);
    void removeById(Long id);
}
