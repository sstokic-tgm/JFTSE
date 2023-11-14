package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.map.SMaps;
import com.jftse.entities.database.repository.map.MapsRepository;
import com.jftse.server.core.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class MapServiceImpl implements MapService {
    private final MapsRepository mapsRepository;

    @Override
    public Optional<SMaps> findByMap(Integer map) {
        return mapsRepository.findByMap(map);
    }
}
