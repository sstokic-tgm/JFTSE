package com.jftse.server.core.service;

import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.ToolSlotEquipment;

import java.util.List;

public interface ToolSlotEquipmentService {
    ToolSlotEquipment save(ToolSlotEquipment toolSlotEquipment);

    ToolSlotEquipment findById(Long id);

    void updateToolSlots(ToolSlotEquipment toolSlotEquipment, Integer toolSlotId);

    void updateToolSlots(Player player, List<Integer> toolSlotItems);

    List<Integer> getEquippedToolSlots(Player player);
}
