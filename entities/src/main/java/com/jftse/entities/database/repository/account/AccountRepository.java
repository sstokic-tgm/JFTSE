package com.jftse.entities.database.repository.account;

import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findAccountByUsername(String username);
    Optional<Account> findAccountById(Long id);
    Optional<Account> findByEmail(String email);
    List<Account> findByStatus(Integer status);
    List<Account> findByStatusAndLoggedInServer(Integer status, ServerType loggedInServer);
}
