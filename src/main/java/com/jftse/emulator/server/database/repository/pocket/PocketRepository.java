package com.jftse.emulator.server.database.repository.pocket;

import com.jftse.emulator.server.database.model.pocket.Pocket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PocketRepository extends JpaRepository<Pocket, Long> {
    // empty..
}
