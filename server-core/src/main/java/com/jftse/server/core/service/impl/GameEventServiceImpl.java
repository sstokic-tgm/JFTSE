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
public class GameEventServiceImpl implements GameEventService {
    private final SGameEventRepository gameEventRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<SGameEvent> findById(Long id) {
        return gameEventRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SGameEvent> findByName(String name) {
        return gameEventRepository.findByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SGameEvent> findAll() {
        return gameEventRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SGameEvent> findAllEnabled() {
        return gameEventRepository.findAllByEnabledTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SGameEvent> findAllByType(String type) {
        return findAllByType(type);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SGameEvent save(SGameEvent gameEvent) {
        return gameEventRepository.save(gameEvent);
    }

    @Override
    @Transactional
    public void remove(SGameEvent gameEvent) {
        gameEventRepository.delete(gameEvent);
    }

    @Override
    @Transactional
    public void removeById(Long id) {
        gameEventRepository.deleteById(id);
    }
}
