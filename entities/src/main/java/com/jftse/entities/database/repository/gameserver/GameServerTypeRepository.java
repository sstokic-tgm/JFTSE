package com.jftse.entities.database.repository.gameserver;

import com.jftse.entities.database.model.gameserver.GameServerType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameServerTypeRepository extends JpaRepository<GameServerType, Long> {
    // empty..
}
