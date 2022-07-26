package com.jftse.entities.database.repository.gameserver;

import com.jftse.entities.database.model.gameserver.GameServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameServerRepository extends JpaRepository<GameServer, Long> {
    @Query(value = "FROM GameServer gs LEFT JOIN FETCH gs.gameServerType gst WHERE gst.type IS NOT NULL")
    List<GameServer> findAllFetched();
    @Query(value = "FROM GameServer gs LEFT JOIN FETCH gs.gameServerType gst WHERE gs.port = :port")
    Optional<GameServer> findGameServerByPort(@Param("port") Integer port);
}
