package com.jftse.server.core.service.impl;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.gameserver.GameServer;
import com.jftse.entities.database.repository.account.AccountRepository;
import com.jftse.entities.database.repository.gameserver.GameServerRepository;
import com.jftse.server.core.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
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
    private final PasswordEncoder passwordEncoder;

    private final ConfigService configService;
    private boolean passwordEncryptionEnabled;

    @PostConstruct
    public void init() {
        passwordEncryptionEnabled = configService.getValue("password.encryption.enabled", false);
    }

    @Override
    public int login(String username, String password) {
        Optional<Account> optionalAccount = accountRepository.findAccountByUsername(username);
        if (optionalAccount.isPresent() && optionalAccount.get().getUsername().equals(username)) {
            Account account = optionalAccount.get();

            if (passwordEncryptionEnabled) {
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
    public int checkPassword(String expectedPassword, String actualPassword) {
        if (passwordEncryptionEnabled) {
            if (passwordEncoder.matches(actualPassword, expectedPassword))
                return SUCCESS;
            else
                return ACCOUNT_INVALID_PASSWORD;
        } else {
            if (actualPassword.equals(expectedPassword))
                return SUCCESS;
            else
                return ACCOUNT_INVALID_PASSWORD;
        }
    }

    @Override
    public Account updateAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Account findAccountByUsername(String username) {
        Optional<Account> account = accountRepository.findAccountByUsername(username);
        if (account.isPresent() && account.get().getUsername().equals(username))
            return account.get();
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Account findAccountById(Long id) {
        Optional<Account> account = accountRepository.findAccountById(id);
        return account.orElse(null);
    }

    @Transactional(readOnly = true)
    public Account getAccountRef(Long accountId) {
        return accountRepository.getReferenceById(accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> findByStatus(Integer status) {
        return accountRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> findByStatusAndLoggedInServer(Integer status, ServerType loggedInServer) {
        return accountRepository.findByStatusAndLoggedInServer(status, loggedInServer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameServer> getGameServerList() {
        return gameServerRepository.findAllFetched();
    }

    @Override
    @Transactional(readOnly = true)
    public GameServer getGameServerByPort(Integer port) {
        Optional<GameServer> gameServer = gameServerRepository.findGameServerByPort(port);
        return gameServer.orElse(null);
    }
}
