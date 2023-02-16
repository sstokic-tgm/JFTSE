package com.jftse.server.core.service;

import com.jftse.entities.database.model.pocket.Pocket;

public interface PocketService {
    Pocket save(Pocket pocket);

    Pocket findById(Long pocketId);

    Pocket incrementPocketBelongings(Pocket pocket);

    Pocket decrementPocketBelongings(Pocket pocket);
}
