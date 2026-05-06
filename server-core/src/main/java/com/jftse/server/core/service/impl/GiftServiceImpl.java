package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.messenger.Gift;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.messenger.GiftRepository;
import com.jftse.server.core.service.GiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GiftServiceImpl implements GiftService {
    private final GiftRepository giftRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Gift save(Gift gift) {
        return giftRepository.save(gift);
    }

    @Override
    @Transactional
    public void remove(Long giftId) {
        giftRepository.findById(giftId).ifPresent(g -> giftRepository.deleteById(g.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Gift findById(Long id) {
        return giftRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Gift> findBySender(Player sender) {
        return giftRepository.findBySender(sender);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Gift> findWithPlayerBySender(Long senderId) {
        return giftRepository.findWithPlayerBySender(senderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Gift> findByReceiver(Player receiver) {
        return giftRepository.findByReceiver(receiver);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Gift> findWithPlayerByReceiver(Long receiverId) {
        return giftRepository.findWithPlayerByReceiver(receiverId);
    }

    @Override
    @Transactional
    public long deleteBySender(Player sender) {
        return giftRepository.deleteBySender(sender);
    }

    @Override
    @Transactional
    public long deleteByReceiver(Player receiver) {
        return giftRepository.deleteByReceiver(receiver);
    }
}
