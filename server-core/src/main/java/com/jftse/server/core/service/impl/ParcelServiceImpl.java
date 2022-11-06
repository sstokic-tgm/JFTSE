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
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ParcelServiceImpl implements ParcelService {
    private final ParcelRepository parcelRepository;

    @Override
    public Parcel save(Parcel parcel) {
        return parcelRepository.save(parcel);
    }

    @Override
    public void remove(Long parcelId) {
        parcelRepository.deleteById(parcelId);
    }

    @Override
    public Parcel findById(Long id) { return parcelRepository.findById(id).orElse(null); }

    @Override
    public List<Parcel> findBySender(Player sender) { return parcelRepository.findBySender(sender); }

    @Override
    public List<Parcel> findByReceiver(Player receiver) { return parcelRepository.findByReceiver(receiver); }

    @Override
    public long deleteBySender(Player sender) {
        return parcelRepository.deleteBySender(sender);
    }

    @Override
    public long deleteByReceiver(Player receiver) {
        return parcelRepository.deleteByReceiver(receiver);
    }
}
