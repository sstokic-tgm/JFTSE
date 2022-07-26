package com.jftse.entities.database.repository.pocket;

import com.jftse.entities.database.model.pocket.Pocket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PocketRepository extends JpaRepository<Pocket, Long> {
    // empty..
}
