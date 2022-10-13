package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.messenger.Message;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.messenger.MessageRepository;
import com.jftse.server.core.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;

    @Override
    public Message save(Message message) {
        return messageRepository.save(message);
    }

    @Override
    public void remove(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    @Override
    public Message findById(Long id) { return messageRepository.findById(id).get(); }

    @Override
    public List<Message> findBySender(Player sender) { return messageRepository.findBySender(sender); }

    @Override
    public List<Message> findByReceiver(Player receiver) { return messageRepository.findByReceiver(receiver); }

    @Override
    public long deleteBySender(Player sender) {
        return messageRepository.deleteBySender(sender);
    }

    @Override
    public long deleteByReceiver(Player receiver) {
        return messageRepository.deleteByReceiver(receiver);
    }
}
