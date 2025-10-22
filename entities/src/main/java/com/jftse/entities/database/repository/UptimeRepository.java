package com.jftse.entities.database.repository;

import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.Uptime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UptimeRepository extends JpaRepository<Uptime, Long> {
    @Modifying
    @Query("UPDATE Uptime u SET u.uptime = :uptime, u.maxPlayers = :maxPlayers WHERE u.serverType = :serverType AND u.startTime = :startTime")
    void updateUptimeAndMaxPlayers(Long uptime, Integer maxPlayers, ServerType serverType, Long startTime);
}
