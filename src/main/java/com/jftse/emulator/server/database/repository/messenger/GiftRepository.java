package com.jftse.emulator.server.database.repository.messenger;

import com.jftse.emulator.server.database.model.messenger.Gift;
import com.jftse.emulator.server.database.model.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GiftRepository extends JpaRepository<Gift, Long> {
    Optional<Gift> findById(Long id);
    List<Gift> findBySender(Player sender);
    List<Gift> findByReceiver(Player receiver);
    long deleteBySender(Player sender);
    long deleteByReceiver(Player receiver);
}