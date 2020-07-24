package com.ft.emulator.server.database.repository.account;

import com.ft.emulator.server.database.model.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query(value = "FROM Account a LEFT JOIN FETCH a.playerList pl WHERE a.username = :username AND a.password = :password")
    List<Account> findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    @Query(value = "FROM Account a LEFT JOIN FETCH a.playerList pl WHERE a.username = :username")
    Optional<Account> findAccountByUsername(@Param("username") String username);

    @Query(value = "FROM Account a LEFT JOIN FETCH a.playerList pl WHERE a.id = :id")
    Optional<Account> findAccountById(@Param("id") Long id);
}