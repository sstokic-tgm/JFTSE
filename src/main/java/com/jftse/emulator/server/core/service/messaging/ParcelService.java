package com.jftse.emulator.server.core.service.messaging;

import com.jftse.emulator.server.database.model.messaging.Parcel;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.repository.messaging.ParcelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ParcelService {
    private final ParcelRepository parcelRepository;

    public Parcel save(Parcel parcel) {
        return parcelRepository.save(parcel);
    }

    public void remove(Long parcelId) {
        parcelRepository.deleteById(parcelId);
    }

    public Parcel findById(Long id) { return parcelRepository.findById(id).get(); }

    public List<Parcel> findBySender(Player sender) { return parcelRepository.findBySender(sender); }

    public List<Parcel> findByReceiver(Player receiver) { return parcelRepository.findByReceiver(receiver); }

    public long deleteBySender(Player sender) {
        return parcelRepository.deleteBySender(sender);
    }

    public long deleteByReceiver(Player receiver) {
        return parcelRepository.deleteByReceiver(receiver);
    }
}
