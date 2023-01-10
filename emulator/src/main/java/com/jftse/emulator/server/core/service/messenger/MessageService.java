package com.jftse.emulator.server.core.service.messenger;

import com.jftse.entities.database.model.messenger.Message;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.messenger.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class MessageService {
    private final MessageRepository messageRepository;

    public Message save(Message message) {
        return messageRepository.save(message);
    }

    public void remove(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    public Message findById(Long id) { return messageRepository.findById(id).get(); }

    public List<Message> findBySender(Player sender) { return messageRepository.findBySender(sender); }

    public List<Message> findByReceiver(Player receiver) { return messageRepository.findByReceiver(receiver); }

    public long deleteBySender(Player sender) {
        return messageRepository.deleteBySender(sender);
    }

    public long deleteByReceiver(Player receiver) {
        return messageRepository.deleteByReceiver(receiver);
    }
}
