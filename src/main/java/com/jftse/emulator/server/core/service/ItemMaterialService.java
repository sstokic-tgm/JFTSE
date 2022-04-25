package com.jftse.emulator.server.core.service;

import com.jftse.emulator.server.database.model.item.ItemMaterial;
import com.jftse.emulator.server.database.repository.item.ItemMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ItemMaterialService {
    private final ItemMaterialRepository itemMaterialRepository;

    public List<Integer> findAllItemIndexes() {
        return itemMaterialRepository.findAllItemIndexes();
    }
}
