package com.jftse.server.core.service;

import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.QuickSlotEquipment;

import java.util.List;

public interface QuickSlotEquipmentService {
    QuickSlotEquipment save(QuickSlotEquipment quickSlotEquipment);

    QuickSlotEquipment findById(Long id);

    void updateQuickSlots(QuickSlotEquipment quickSlotEquipment, Integer quickSlotId);

    void updateQuickSlots(Player player, List<Integer> quickSlotItems);

    List<Integer> getEquippedQuickSlots(Player player);
}
