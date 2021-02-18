package com.jftse.emulator.server.database.repository.home;

import com.jftse.emulator.server.database.model.home.AccountHome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountHomeRepository extends JpaRepository<AccountHome, Long> {
    @Query(value = "FROM AccountHome ah LEFT JOIN FETCH ah.account account WHERE account.id = :accountId")
    Optional<AccountHome> findAccountHomeByAccountId(@Param("accountId") Long accountId);
}
