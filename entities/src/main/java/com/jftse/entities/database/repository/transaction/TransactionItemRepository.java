package com.jftse.entities.database.repository.transaction;

import com.jftse.entities.database.model.transaction.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionItemRepository extends JpaRepository<TransactionItem, Long> {
    Optional<TransactionItem> findFirstByPocketIdAndReferenceIdOrderByIdDesc(Long pocketId, Long referenceId);
}
