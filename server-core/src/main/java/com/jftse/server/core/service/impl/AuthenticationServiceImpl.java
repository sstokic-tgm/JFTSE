package com.jftse.server.core.service.impl;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.gameserver.GameServer;
import com.jftse.entities.database.repository.account.AccountRepository;
import com.jftse.entities.database.repository.gameserver.GameServerRepository;
import com.jftse.server.core.service.AuthenticationService;
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
public class AuthenticationServiceImpl implements AuthenticationService {
    public final static short SUCCESS = 0;
    public final static short ACCOUNT_INVALID_PASSWORD = -1;
    public final static short ACCOUNT_ALREADY_LOGGED_IN = -2;
    public final static short ACCOUNT_EXPIRED_USER_ID = -3;
    public final static short ACCOUNT_INVALID_USER_ID = -4;
    public final static short ACCOUNT_BLOCKED_USER_ID = -6;
    public final static short INVAILD_VERSION = -62;
    
    private final AccountRepository accountRepository;
    private final GameServerRepository gameServerRepository;

    private final ConfigService configService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2Y, 12);
    }

    @Override
    public int login(String username, String password) {
        Optional<Account> optionalAccount = accountRepository.findAccountByUsername(username);
        if (optionalAccount.isPresent() && optionalAccount.get().getUsername().equals(username)) {
            Account account = optionalAccount.get();

            if (configService.getValue("password.encryption.enabled", false)) {
                if (passwordEncoder.matches(password, account.getPassword()))
                    return SUCCESS;
                else
                    return ACCOUNT_INVALID_PASSWORD;
            } else {
                if (password.equals(account.getPassword()))
                    return SUCCESS;
                else
                    return ACCOUNT_INVALID_PASSWORD;
            }
        } else
            return ACCOUNT_INVALID_USER_ID;
    }

    @Override
    public Account updateAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public Account findAccountByUsername(String username) {
        Optional<Account> account = accountRepository.findAccountByUsername(username);
        if (account.isPresent() && account.get().getUsername().equals(username))
            return account.get();
        return null;
    }

    @Override
    public Account findAccountById(Long id) {
        Optional<Account> account = accountRepository.findAccountById(id);
        return account.orElse(null);
    }

    @Override
    public List<GameServer> getGameServerList() {
        return gameServerRepository.findAllFetched();
    }

    @Override
    public GameServer getGameServerByPort(Integer port) {
        Optional<GameServer> gameServer = gameServerRepository.findGameServerByPort(port);
        return gameServer.orElse(null);
    }
}
