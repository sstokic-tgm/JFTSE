package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.database.model.anticheat.ClientWhitelist;
import com.jftse.emulator.server.database.repository.anticheat.ClientWhitelistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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

    public ClientWhitelist findByIpAndHwid(String ip, String hwid) {
        try {
            Optional<ClientWhitelist> clientWhitelist = clientWhitelistRepository.findByIpAndHwid(ip, hwid, Sort.by("created").descending());
            return clientWhitelist.orElse(null);
        } catch (Exception e) {
            List<ClientWhitelist> clientWhitelist = clientWhitelistRepository.findAllByIpAndHwid(ip, hwid, Sort.by("created").descending());
            return clientWhitelist.isEmpty() ? null : clientWhitelist.get(0);
        }
    }

    public ClientWhitelist findByIpAndHwidAndFlaggedTrue(String ip, String hwid) {
        try {
            Optional<ClientWhitelist> clientWhitelist = clientWhitelistRepository.findByIpAndHwidAndFlaggedTrue(ip, hwid, Sort.by("created").descending());
            return clientWhitelist.orElse(null);
        } catch (Exception e) {
            List<ClientWhitelist> clientWhitelist = clientWhitelistRepository.findAllByIpAndHwidAndFlaggedTrue(ip, hwid, Sort.by("created").descending());
            return clientWhitelist.isEmpty() ? null : clientWhitelist.get(0);
        }
    }

    public ClientWhitelist findByIpAndHwidAndAccount(String ip, String hwid, Account account) {
        try {
            Optional<ClientWhitelist> clientWhitelist = clientWhitelistRepository.findByIpAndHwidAndAccount(ip, hwid, account, Sort.by("created").descending());
            return clientWhitelist.orElse(null);
        } catch (Exception e) {
            List<ClientWhitelist> clientWhitelist = clientWhitelistRepository.findAllByIpAndHwidAndAccount(ip, hwid, account, Sort.by("created").descending());
            return clientWhitelist.isEmpty() ? null : clientWhitelist.get(0);
        }
    }

    public List<ClientWhitelist> findAll() {
        return clientWhitelistRepository.findAll();
    }

    public void remove(Long clientWhitelistId) {
        clientWhitelistRepository.deleteById(clientWhitelistId);
    }
}