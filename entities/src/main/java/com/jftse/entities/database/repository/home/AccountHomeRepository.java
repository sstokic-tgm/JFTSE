package com.jftse.entities.database.repository.home;

import com.jftse.entities.database.model.home.AccountHome;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountHomeRepository extends JpaRepository<AccountHome, Long> {
    Optional<AccountHome> findByAccountId(Long accountId);

    @EntityGraph(attributePaths = { "inventoryItems" })
    Optional<AccountHome> findWithInventoryById(Long id);

    @EntityGraph(attributePaths = { "account" })
    Optional<AccountHome> findWithAccountById(Long id);
}
