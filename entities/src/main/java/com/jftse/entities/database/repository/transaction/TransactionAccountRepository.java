package com.jftse.entities.database.repository.transaction;

import com.jftse.entities.database.model.account.Account;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.Repository;
import javax.persistence.LockModeType;
import java.util.Optional;

public interface TransactionAccountRepository extends Repository<Account, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findById(Long id);
}
