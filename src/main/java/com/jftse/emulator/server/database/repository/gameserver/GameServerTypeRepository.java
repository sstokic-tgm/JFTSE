package com.jftse.emulator.server.database.repository.gameserver;

import com.jftse.emulator.server.database.model.gameserver.GameServerType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameServerTypeRepository extends JpaRepository<GameServerType, Long> {
    // empty..
}
