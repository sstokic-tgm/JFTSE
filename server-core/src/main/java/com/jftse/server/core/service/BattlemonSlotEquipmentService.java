package com.jftse.server.core.service;

import com.jftse.entities.database.model.player.BattlemonSlotEquipment;
import com.jftse.entities.database.model.player.Player;

import java.util.List;

public interface BattlemonSlotEquipmentService {
    BattlemonSlotEquipment save(BattlemonSlotEquipment battlemonSlotEquipment);

    BattlemonSlotEquipment findById(Long id);

    void updateBattlemonSlots(BattlemonSlotEquipment battlemonSlotEquipment, Integer battlemonSlotId);

    void updateBattlemonSlots(BattlemonSlotEquipment battlemonSlotEquipment, List<Integer> battlemonSlotItems);

    List<Integer> getEquippedBattlemonSlots(Player player);
}
