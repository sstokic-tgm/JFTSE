package com.jftse.server.core.service;

import com.jftse.entities.database.model.item.ItemSpecial;

public interface ItemSpecialService {
    ItemSpecial findByItemIndex(Integer itemIndex);
}
