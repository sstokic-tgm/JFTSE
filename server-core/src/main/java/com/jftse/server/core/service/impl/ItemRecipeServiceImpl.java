package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.item.ItemRecipe;
import com.jftse.entities.database.repository.item.ItemRecipeRepository;
import com.jftse.server.core.service.ItemRecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ItemRecipeServiceImpl implements ItemRecipeService {
    private final ItemRecipeRepository itemRecipeRepository;

    @Override
    public ItemRecipe findById(Long id) {
        Optional<ItemRecipe> itemRecipe = itemRecipeRepository.findById(id);
        return  itemRecipe.orElse(null);
    }

    @Override
    public ItemRecipe findByItemIndex(Integer itemIndex) {
        Optional<ItemRecipe> itemRecipe = itemRecipeRepository.findByItemIndex(itemIndex);
        return itemRecipe.orElse(null);
    }
}
