package com.jftse.server.core.service;

import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.SpecialSlotEquipment;

import java.util.List;

public interface SpecialSlotEquipmentService {
    SpecialSlotEquipment save(SpecialSlotEquipment specialSlotEquipment);

    SpecialSlotEquipment findById(Long id);

    void updateSpecialSlots(SpecialSlotEquipment specialSlotEquipment, Integer specialSlotId);

    void updateSpecialSlots(Player player, List<Integer> specialSlotItems);

    List<Integer> getEquippedSpecialSlots(Player player);
}
