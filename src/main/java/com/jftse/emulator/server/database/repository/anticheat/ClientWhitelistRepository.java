package com.jftse.emulator.server.database.repository.anticheat;

import com.jftse.emulator.server.database.model.anticheat.ClientWhitelist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientWhitelistRepository extends JpaRepository<ClientWhitelist, Long> {
    Optional<ClientWhitelist> findByIp(String ip);
}