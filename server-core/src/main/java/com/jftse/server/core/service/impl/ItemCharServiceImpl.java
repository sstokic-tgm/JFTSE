package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.item.ItemChar;
import com.jftse.entities.database.repository.item.ItemCharRepository;
import com.jftse.server.core.service.ItemCharService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemCharServiceImpl implements ItemCharService {

    private final ItemCharRepository itemCharRepository;

    @Override
    public ItemChar findByPlayerType(byte playerType) {
        Optional<ItemChar> itemChar = itemCharRepository.findByPlayerType(playerType);
        return itemChar.orElse(null);
    }
}