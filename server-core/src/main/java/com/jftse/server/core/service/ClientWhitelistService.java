package com.jftse.server.core.service;

import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.anticheat.ClientWhitelist;

import java.util.List;

public interface ClientWhitelistService {
    ClientWhitelist save(ClientWhitelist clientWhitelist);

    ClientWhitelist findByIpAndHwid(String ip, String hwid);

    ClientWhitelist findByHwidAndFlaggedTrue(String hwid);

    ClientWhitelist findByIpAndHwidAndAccount(String ip, String hwid, Account account);

    List<ClientWhitelist> findAll();

    void remove(Long clientWhitelistId);
}
