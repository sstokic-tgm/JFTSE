package com.jftse.emulator.server.core.service;

import com.jftse.entities.database.model.item.ItemSpecial;
import com.jftse.entities.database.repository.item.ItemSpecialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ItemSpecialService {
    private final ItemSpecialRepository itemSpecialRepository;

    public ItemSpecial findByItemIndex(Integer itemIndex) {
        Optional<ItemSpecial> itemSpecial = itemSpecialRepository.findByItemIndex(itemIndex);
        return itemSpecial.orElse(null);
    }
}
