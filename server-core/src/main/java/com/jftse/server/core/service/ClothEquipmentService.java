package com.jftse.server.core.service;

import com.jftse.entities.database.model.player.ClothEquipment;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.EquippedItemStats;

import java.util.Map;

public interface ClothEquipmentService {
    ClothEquipment save(ClothEquipment clothEquipment);

    ClothEquipment findClothEquipmentById(Long id);

    Map<String, Integer> getEquippedCloths(Player player);

    EquippedItemStats getStatusPointsFromCloths(Player player);
}
