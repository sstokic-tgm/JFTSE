package com.ft.emulator.server.database.repository.gameserver;

import com.ft.emulator.server.database.model.gameserver.GameServerType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameServerTypeRepository extends JpaRepository<GameServerType, Long> {
}