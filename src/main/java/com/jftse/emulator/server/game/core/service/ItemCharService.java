package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.server.database.model.item.ItemChar;
import com.jftse.emulator.server.database.repository.item.ItemCharRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ItemCharService {

    private final ItemCharRepository itemCharRepository;

    public ItemChar findByPlayerType(byte playerType) {
        Optional<ItemChar> itemChar = itemCharRepository.findByPlayerType(playerType);
        return itemChar.orElse(null);
    }
}