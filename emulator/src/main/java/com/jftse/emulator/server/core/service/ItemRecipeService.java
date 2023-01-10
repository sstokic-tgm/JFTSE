package com.jftse.emulator.server.core.service;

import com.jftse.entities.database.model.item.ItemRecipe;
import com.jftse.entities.database.repository.item.ItemRecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ItemRecipeService {
    private final ItemRecipeRepository itemRecipeRepository;

    public ItemRecipe findById(Long id) {
        Optional<ItemRecipe> itemRecipe = itemRecipeRepository.findById(id);
        return  itemRecipe.orElse(null);
    }

    public ItemRecipe findByItemIndex(Integer itemIndex) {
        Optional<ItemRecipe> itemRecipe = itemRecipeRepository.findByItemIndex(itemIndex);
        return itemRecipe.orElse(null);
    }
}
