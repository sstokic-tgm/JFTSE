package com.jftse.emulator.server.database.repository.account;

import com.jftse.emulator.server.database.model.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findAccountByUsername(String username);
    Optional<Account> findAccountById(Long id);
}
