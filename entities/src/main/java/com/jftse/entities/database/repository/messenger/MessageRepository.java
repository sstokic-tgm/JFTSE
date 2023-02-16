package com.jftse.entities.database.repository.messenger;

import com.jftse.entities.database.model.messenger.Message;
import com.jftse.entities.database.model.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findById(Long id);
    List<Message> findBySender(Player sender);
    List<Message> findByReceiver(Player receiver);
    long deleteBySender(Player sender);
    long deleteByReceiver(Player receiver);
}