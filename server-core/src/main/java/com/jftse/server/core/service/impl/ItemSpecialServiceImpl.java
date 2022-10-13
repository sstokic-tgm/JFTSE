package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.item.ItemSpecial;
import com.jftse.entities.database.repository.item.ItemSpecialRepository;
import com.jftse.server.core.service.ItemSpecialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ItemSpecialServiceImpl implements ItemSpecialService {
    private final ItemSpecialRepository itemSpecialRepository;

    @Override
    public ItemSpecial findByItemIndex(Integer itemIndex) {
        Optional<ItemSpecial> itemSpecial = itemSpecialRepository.findByItemIndex(itemIndex);
        return itemSpecial.orElse(null);
    }
}
