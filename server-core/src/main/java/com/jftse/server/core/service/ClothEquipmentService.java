package com.jftse.server.core.service;

import com.jftse.entities.database.model.player.ClothEquipment;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;

import java.util.Map;

public interface ClothEquipmentService {
    ClothEquipment save(ClothEquipment clothEquipment);

    ClothEquipment findClothEquipmentById(Long id);

    Map<String, Integer> getEquippedCloths(Player player);

    StatusPointsAddedDto getStatusPointsFromCloths(Player player);
}
