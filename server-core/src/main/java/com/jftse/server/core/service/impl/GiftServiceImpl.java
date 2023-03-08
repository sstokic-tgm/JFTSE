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
@Transactional(isolation = Isolation.SERIALIZABLE)
public class GiftServiceImpl implements GiftService {
    private final GiftRepository giftRepository;

    @Override
    public Gift save(Gift gift) {
        return giftRepository.save(gift);
    }

    @Override
    public void remove(Long giftId) {
        giftRepository.findById(giftId).ifPresent(g -> giftRepository.deleteById(g.getId()));
    }

    @Override
    public Gift findById(Long id) { return giftRepository.findById(id).orElse(null); }

    @Override
    public List<Gift> findBySender(Player sender) { return giftRepository.findBySender(sender); }

    @Override
    public List<Gift> findByReceiver(Player receiver) { return giftRepository.findByReceiver(receiver); }

    @Override
    public long deleteBySender(Player sender) {
        return giftRepository.deleteBySender(sender);
    }

    @Override
    public long deleteByReceiver(Player receiver) {
        return giftRepository.deleteByReceiver(receiver);
    }
}
