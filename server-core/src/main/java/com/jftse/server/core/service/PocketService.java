package com.jftse.server.core.service;

import com.jftse.entities.database.model.pocket.Pocket;

public interface PocketService {
    Pocket save(Pocket pocket);

    Pocket findById(Long pocketId);

    Pocket incrementPocketBelongings(Pocket pocket);
    Pocket incrementPocketBelongings(Long pocketId);

    Pocket decrementPocketBelongings(Pocket pocket);
    Pocket decrementPocketBelongings(Long pocketId);
}
