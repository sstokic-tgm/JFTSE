package com.jftse.entities.database.repository.messenger;

import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.entities.database.model.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
    Optional<Parcel> findById(Long id);
    List<Parcel> findBySender(Player sender);
    List<Parcel> findByReceiver(Player receiver);

    @Query(value = "SELECT p FROM Parcel p JOIN FETCH p.receiver pr WHERE p.sender.id = :senderId")
    List<Parcel> findWithPlayerBySender(Long senderId);

    @Query(value = "SELECT p FROM Parcel p JOIN FETCH p.sender ps WHERE p.receiver.id = :receiverId")
    List<Parcel> findWithPlayerByReceiver(Long receiverId);

    long deleteBySender(Player sender);
    long deleteByReceiver(Player receiver);
}