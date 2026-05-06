package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.messenger.ParcelRepository;
import com.jftse.server.core.service.ParcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParcelServiceImpl implements ParcelService {
    private final ParcelRepository parcelRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Parcel save(Parcel parcel) {
        return parcelRepository.save(parcel);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void remove(Long parcelId) {
        parcelRepository.findById(parcelId).ifPresent(p -> parcelRepository.deleteById(p.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Parcel findById(Long id) {
        return parcelRepository.findByIdFetched(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Parcel> findBySender(Player sender) {
        return parcelRepository.findBySender(sender);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Parcel> findWithPlayerBySender(Long senderId) {
        return parcelRepository.findWithPlayerBySender(senderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Parcel> findByReceiver(Player receiver) {
        return parcelRepository.findByReceiver(receiver);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Parcel> findWithPlayerByReceiver(Long receiverId) {
        return parcelRepository.findWithPlayerByReceiver(receiverId);
    }

    @Override
    @Transactional
    public long deleteBySender(Player sender) {
        return parcelRepository.deleteBySender(sender);
    }

    @Override
    @Transactional
    public long deleteByReceiver(Player receiver) {
        return parcelRepository.deleteByReceiver(receiver);
    }
}
