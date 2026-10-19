package com.jftse.entities.database.repository.transaction;

import com.jftse.entities.database.model.transaction.TransactionItemComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionItemComponentRepository extends JpaRepository<TransactionItemComponent, Long> {
    List<TransactionItemComponent> findAllByComponentOfIdOrderByIdAsc(Long componentOfId);

    List<TransactionItemComponent> findAllByItemIdOrderByIdAsc(Long itemId);
}
