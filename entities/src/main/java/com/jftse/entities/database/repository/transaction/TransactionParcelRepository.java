package com.jftse.entities.database.repository.transaction;

import com.jftse.entities.database.model.messenger.Parcel;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface TransactionParcelRepository extends Repository<Parcel, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Parcel> findById(Long id);
}
