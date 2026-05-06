package com.jftse.entities.database.repository.messenger;

import com.jftse.entities.database.model.messenger.Message;
import com.jftse.entities.database.model.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySender(Player sender);
    List<Message> findByReceiver(Player receiver);

    @Query(value = "SELECT m FROM Message m JOIN FETCH m.receiver mr WHERE m.sender.id = :senderId")
    List<Message> findWithPlayerBySender(Long senderId);

    @Query(value = "SELECT m FROM Message m JOIN FETCH m.sender ms WHERE m.receiver.id = :receiverId")
    List<Message> findWithPlayerByReceiver(Long receiverId);

    long deleteBySender(Player sender);
    long deleteByReceiver(Player receiver);
}