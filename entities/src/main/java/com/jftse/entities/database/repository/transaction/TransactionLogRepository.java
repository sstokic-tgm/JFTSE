package com.jftse.entities.database.repository.transaction;

import com.jftse.entities.database.model.transaction.TransactionLog;
import com.jftse.entities.database.model.transaction.TransactionType;
import com.jftse.entities.database.model.transaction.TransactionAction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {
    Optional<TransactionLog> findByOperationId(String operationId);

    List<TransactionLog> findAllByAccountIdOrderByCreatedDescIdDesc(Long accountId, Pageable pageable);

    List<TransactionLog> findAllByPlayerIdOrderByCreatedDescIdDesc(Long playerId, Pageable pageable);

    Optional<TransactionLog> findFirstByTypeAndSourceIdAndActionOrderByIdAsc(TransactionType type, Long sourceId, TransactionAction action);

    List<TransactionLog> findAllByComponentOfIdOrderByIdAsc(Long componentOfId);

    List<TransactionLog> findAllByTypeAndSourceIdOrderByIdAsc(TransactionType type, Long sourceId);

    @Query("SELECT t FROM TransactionLog t WHERE t.accountId = :accountId OR t.counterpartyAccountId = :accountId ORDER BY t.created DESC, t.id DESC")
    List<TransactionLog> findAllByParticipantAccountId(@Param("accountId") Long accountId, Pageable pageable);
}
