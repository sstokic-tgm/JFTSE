package com.jftse.server.core.service;

import com.jftse.entities.database.model.item.ItemChar;

public interface ItemCharService {
    ItemChar findByPlayerType(byte playerType);
}
