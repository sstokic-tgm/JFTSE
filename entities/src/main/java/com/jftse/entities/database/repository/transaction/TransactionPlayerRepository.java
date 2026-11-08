package com.jftse.entities.database.repository.transaction;

import com.jftse.entities.database.model.player.Player;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import javax.persistence.LockModeType;
import java.util.Optional;

public interface TransactionPlayerRepository extends Repository<Player, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Player p JOIN FETCH p.pocket WHERE p.id = :id AND p.account.id = :accountId")
    Optional<Player> findForTransaction(@Param("id") Long id, @Param("accountId") Long accountId);
}
