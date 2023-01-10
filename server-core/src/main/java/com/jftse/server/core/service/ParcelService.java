package com.jftse.server.core.service;

import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.entities.database.model.player.Player;

import java.util.List;

public interface ParcelService {
    Parcel save(Parcel parcel);

    void remove(Long parcelId);

    Parcel findById(Long id);

    List<Parcel> findBySender(Player sender);

    List<Parcel> findByReceiver(Player receiver);

    long deleteBySender(Player sender);

    long deleteByReceiver(Player receiver);
}
