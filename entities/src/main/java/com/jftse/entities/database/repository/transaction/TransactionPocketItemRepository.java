package com.jftse.entities.database.repository.transaction;

import com.jftse.entities.database.model.pocket.PlayerPocket;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.Repository;
import javax.persistence.LockModeType;
import java.util.Optional;

public interface TransactionPocketItemRepository extends Repository<PlayerPocket, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PlayerPocket> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PlayerPocket> findByIdAndPocketId(Long id, Long pocketId);
}
