package com.jftse.entities.database.repository.map;

import com.jftse.entities.database.model.map.SMaps;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MapsRepository extends JpaRepository<SMaps, Long> {
    List<SMaps> findAllByNameLike(String name);
    List<SMaps> findAllByIsBossStage(Boolean isBossStage);
    Optional<SMaps> findByMap(Integer map);
    Optional<SMaps> findByMapAndIsBossStage(Integer map, Boolean isBossStage);
}
