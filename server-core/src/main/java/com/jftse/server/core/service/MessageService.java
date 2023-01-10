package com.jftse.server.core.service;

import com.jftse.entities.database.model.messenger.Message;
import com.jftse.entities.database.model.player.Player;

import java.util.List;

public interface MessageService {
    Message save(Message message);

    void remove(Long messageId);

    Message findById(Long id);

    List<Message> findBySender(Player sender);

    List<Message> findByReceiver(Player receiver);

    long deleteBySender(Player sender);

    long deleteByReceiver(Player receiver);
}
