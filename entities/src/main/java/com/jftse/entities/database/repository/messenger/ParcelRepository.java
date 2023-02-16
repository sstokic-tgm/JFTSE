package com.jftse.entities.database.repository.messenger;

import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.entities.database.model.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
    Optional<Parcel> findById(Long id);
    List<Parcel> findBySender(Player sender);
    List<Parcel> findByReceiver(Player receiver);
    long deleteBySender(Player sender);
    long deleteByReceiver(Player receiver);
}