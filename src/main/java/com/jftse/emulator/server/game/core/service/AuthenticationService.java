package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.database.model.gameserver.GameServer;
import com.jftse.emulator.server.database.repository.account.AccountRepository;
import com.jftse.emulator.server.database.repository.gameserver.GameServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class AuthenticationService {
    private final AccountRepository accountRepository;
    private final GameServerRepository gameServerRepository;

    public Account login(String username, String password) {
        List<Account> accountList = accountRepository.findByUsernameAndPassword(username, password);

        return (accountList == null || accountList.isEmpty()) ? null : accountList.get(0);
    }

    public Account updateAccount(Account account) {
        return accountRepository.save(account);
    }

    public Account findAccountByUsername(String username) {
        Optional<Account> account = accountRepository.findAccountByUsername(username);
        return account.orElse(null);
    }

    public Account findAccountById(Long id) {
        Optional<Account> account = accountRepository.findAccountById(id);
        return account.orElse(null);
    }

    public List<GameServer> getGameServerList() {
        return gameServerRepository.findAllFetched();
    }

    public GameServer getGameServerByPort(Integer port) {
        Optional<GameServer> gameServer = gameServerRepository.findGameServerByPort(port);
        return gameServer.orElse(null);
    }
}
