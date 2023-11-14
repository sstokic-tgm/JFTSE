package com.jftse.server.core.service;

import com.jftse.entities.database.model.map.SMaps;

import java.util.Optional;

public interface MapService {
    Optional<SMaps> findByMap(Integer map);
}
