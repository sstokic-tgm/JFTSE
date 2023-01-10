package com.jftse.server.core.service;

import com.jftse.entities.database.model.item.ItemRecipe;

public interface ItemRecipeService {
    ItemRecipe findById(Long id);

    ItemRecipe findByItemIndex(Integer itemIndex);
}
