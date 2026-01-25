package com.jftse.server.core.service;

import com.jftse.entities.database.model.messenger.Gift;
import com.jftse.entities.database.model.player.Player;

import java.util.List;

public interface GiftService {
    Gift save(Gift gift);

    void remove(Long giftId);

    Gift findById(Long id);

    List<Gift> findBySender(Player sender);

    List<Gift> findWithPlayerBySender(Long senderId);

    List<Gift> findByReceiver(Player receiver);

    List<Gift> findWithPlayerByReceiver(Long receiverId);

    long deleteBySender(Player sender);

    long deleteByReceiver(Player receiver);
}
