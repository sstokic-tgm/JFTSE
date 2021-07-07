package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.common.GlobalSettings;
import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.database.model.gameserver.GameServer;
import com.jftse.emulator.server.database.repository.account.AccountRepository;
import com.jftse.emulator.server.database.repository.gameserver.GameServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2Y, 12);
    }

    public Account login(String username, String password) {
        Optional<Account> optionalAccount = accountRepository.findAccountByUsername(username);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();

            if (GlobalSettings.UsePasswordEncryption) {
                if (passwordEncoder.matches(password, account.getPassword()))
                    return account;
                else
                    return null;
            } else {
                if (password.equals(account.getPassword()))
                    return account;
                else
                    return null;
            }
        } else
            return null;
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
