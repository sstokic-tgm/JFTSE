package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.anticheat.ClientWhitelist;
import com.jftse.entities.database.repository.anticheat.ClientWhitelistRepository;
import com.jftse.server.core.service.ClientWhitelistService;
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
public class ClientWhitelistServiceImpl implements ClientWhitelistService {
    private final ClientWhitelistRepository clientWhitelistRepository;

    @Override
    public ClientWhitelist save(ClientWhitelist clientWhitelist) {
        return clientWhitelistRepository.save(clientWhitelist);
    }

    @Override
    public ClientWhitelist findByIpAndHwid(String ip, String hwid) {
        try {
            Optional<ClientWhitelist> clientWhitelist = clientWhitelistRepository.findByIpAndHwid(ip, hwid, Sort.by("created").descending());
            return clientWhitelist.orElse(null);
        } catch (Exception e) {
            List<ClientWhitelist> clientWhitelist = clientWhitelistRepository.findAllByIpAndHwid(ip, hwid, Sort.by("created").descending());
            return clientWhitelist.isEmpty() ? null : clientWhitelist.get(0);
        }
    }

    @Override
    public ClientWhitelist findByHwidAndFlaggedTrue(String hwid) {
        try {
            Optional<ClientWhitelist> clientWhitelist = clientWhitelistRepository.findByHwidAndFlaggedTrue(hwid, Sort.by("created").descending());
            return clientWhitelist.orElse(null);
        } catch (Exception e) {
            List<ClientWhitelist> clientWhitelist = clientWhitelistRepository.findAllByHwidAndFlaggedTrue(hwid, Sort.by("created").descending());
            return clientWhitelist.isEmpty() ? null : clientWhitelist.get(0);
        }
    }

    @Override
    public ClientWhitelist findByIpAndHwidAndAccount(String ip, String hwid, Account account) {
        try {
            Optional<ClientWhitelist> clientWhitelist = clientWhitelistRepository.findByIpAndHwidAndAccount(ip, hwid, account, Sort.by("created").descending());
            return clientWhitelist.orElse(null);
        } catch (Exception e) {
            List<ClientWhitelist> clientWhitelist = clientWhitelistRepository.findAllByIpAndHwidAndAccount(ip, hwid, account, Sort.by("created").descending());
            return clientWhitelist.isEmpty() ? null : clientWhitelist.get(0);
        }
    }

    @Override
    public List<ClientWhitelist> findAll() {
        return clientWhitelistRepository.findAll();
    }

    @Override
    public void remove(Long clientWhitelistId) {
        clientWhitelistRepository.deleteById(clientWhitelistId);
    }
}