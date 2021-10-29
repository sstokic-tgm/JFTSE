package com.jftse.emulator.server.core.service.messaging;

import com.jftse.emulator.server.database.model.messaging.Gift;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.repository.messaging.GiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class GiftService {
    private final GiftRepository giftRepository;

    public Gift save(Gift gift) {
        return giftRepository.save(gift);
    }

    public void remove(Long giftId) {
        giftRepository.deleteById(giftId);
    }

    public Gift findById(Long id) { return giftRepository.findById(id).get(); }

    public List<Gift> findBySender(Player sender) { return giftRepository.findBySender(sender); }

    public List<Gift> findByReceiver(Player receiver) { return giftRepository.findByReceiver(receiver); }

    public long deleteBySender(Player sender) {
        return giftRepository.deleteBySender(sender);
    }

    public long deleteByReceiver(Player receiver) {
        return giftRepository.deleteByReceiver(receiver);
    }
}
