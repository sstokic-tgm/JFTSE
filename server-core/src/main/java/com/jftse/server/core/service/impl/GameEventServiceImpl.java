package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.event.SGameEvent;
import com.jftse.entities.database.repository.event.SGameEventRepository;
import com.jftse.server.core.service.GameEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class GameEventServiceImpl implements GameEventService {
    private final SGameEventRepository gameEventRepository;

    @Override
    public Optional<SGameEvent> findById(Long id) {
        return gameEventRepository.findById(id);
    }

    @Override
    public Optional<SGameEvent> findByName(String name) {
        return gameEventRepository.findByName(name);
    }

    @Override
    public List<SGameEvent> findAll() {
        return gameEventRepository.findAll();
    }

    @Override
    public List<SGameEvent> findAllEnabled() {
        return gameEventRepository.findAllByEnabledTrue();
    }

    @Override
    public List<SGameEvent> findAllByType(String type) {
        return findAllByType(type);
    }

    @Override
    public SGameEvent save(SGameEvent gameEvent) {
        return gameEventRepository.save(gameEvent);
    }

    @Override
    public void remove(SGameEvent gameEvent) {
        gameEventRepository.delete(gameEvent);
    }

    @Override
    public void removeById(Long id) {
        gameEventRepository.deleteById(id);
    }
}
