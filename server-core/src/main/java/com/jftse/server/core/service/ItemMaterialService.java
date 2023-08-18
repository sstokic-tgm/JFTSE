package com.jftse.server.core.service;

import com.jftse.entities.database.model.item.ItemMaterial;

import java.util.List;
import java.util.Optional;

public interface ItemMaterialService {
    List<Integer> findAllItemIndexesDB();

    List<Integer> findAllItemIndexes();

    Optional<ItemMaterial> findByItemIndex(Integer itemIndex);
}
