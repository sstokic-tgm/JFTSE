package com.jftse.server.core.service;

import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.gameserver.GameServer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

public interface AuthenticationService {
    @Bean
    PasswordEncoder passwordEncoder();

    int login(String username, String password);

    Account updateAccount(Account account);

    Account findAccountByUsername(String username);

    Account findAccountById(Long id);

    List<Account> findByStatus(Integer status);

    List<Account> findByStatusAndLoggedInServer(Integer status, ServerType loggedInServer);

    List<GameServer> getGameServerList();

    GameServer getGameServerByPort(Integer port);
}
