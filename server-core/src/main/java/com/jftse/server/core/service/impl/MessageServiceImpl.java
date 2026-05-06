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
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Message save(Message message) {
        return messageRepository.save(message);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void remove(Long messageId) {
        messageRepository.findById(messageId).ifPresent(m -> messageRepository.deleteById(m.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Message findById(Long id) {
        return messageRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findBySender(Player sender) {
        return messageRepository.findBySender(sender);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findWithPlayerBySender(Long senderId) {
        return messageRepository.findWithPlayerBySender(senderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findByReceiver(Player receiver) {
        return messageRepository.findByReceiver(receiver);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findWithPlayerByReceiver(Long receiverId) {
        return messageRepository.findWithPlayerByReceiver(receiverId);
    }

    @Override
    @Transactional
    public long deleteBySender(Player sender) {
        return messageRepository.deleteBySender(sender);
    }

    @Override
    @Transactional
    public long deleteByReceiver(Player receiver) {
        return messageRepository.deleteByReceiver(receiver);
    }
}
