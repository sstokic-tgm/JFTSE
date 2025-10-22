package com.jftse.entities.database.repository;

import com.jftse.entities.database.model.EconomySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EconomySnapshotRepository extends JpaRepository<EconomySnapshot, Long> {
}
