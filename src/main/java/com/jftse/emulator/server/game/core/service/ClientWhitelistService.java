package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.server.database.model.anticheat.ClientWhitelist;
import com.jftse.emulator.server.database.repository.anticheat.ClientWhitelistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ClientWhitelistService {
    private final ClientWhitelistRepository clientWhitelistRepository;

    public ClientWhitelist save(ClientWhitelist clientWhitelist) {
        return clientWhitelistRepository.save(clientWhitelist);
    }

    public ClientWhitelist findByIp(String ip) {
        Optional<ClientWhitelist> clientWhitelist = clientWhitelistRepository.findByIp(ip);
        return clientWhitelist.orElse(null);
    }

    public List<ClientWhitelist> findAll() {
        return clientWhitelistRepository.findAll();
    }

    public void remove(Long clientWhitelistId) {
        clientWhitelistRepository.deleteById(clientWhitelistId);
    }
}