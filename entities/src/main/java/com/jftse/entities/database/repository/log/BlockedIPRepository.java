package com.jftse.entities.database.repository.log;

import com.jftse.entities.database.model.log.BlockedIP;
import com.jftse.entities.database.model.log.ServerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockedIPRepository extends JpaRepository<BlockedIP, Long> {
    Optional<BlockedIP> findBlockedIPByIpAndServerType(String ip, ServerType serverType);
}
