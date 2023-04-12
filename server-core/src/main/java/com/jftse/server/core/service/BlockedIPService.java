package com.jftse.server.core.service;

import com.jftse.entities.database.model.log.BlockedIP;
import com.jftse.entities.database.model.ServerType;

import java.util.List;
import java.util.Optional;

public interface BlockedIPService {
    BlockedIP save(BlockedIP blockedIP);

    void remove(BlockedIP blockedIP);

    Optional<BlockedIP> findBlockedIPByIpAndServerType(String ip, ServerType serverType);

    List<BlockedIP> findAll();
}
