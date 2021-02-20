package com.jftse.emulator.server.database.repository.gameserver;

import com.jftse.emulator.server.database.model.gameserver.GameServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GameServerRepository extends JpaRepository<GameServer, Long> {
    @Query(value = "FROM GameServer gs LEFT JOIN FETCH gs.gameServerType gst")
    List<GameServer> findAllFetched();
}
