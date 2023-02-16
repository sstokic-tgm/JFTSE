package com.jftse.server.core.service;

import com.jftse.entities.database.model.player.CardSlotEquipment;
import com.jftse.entities.database.model.player.Player;

import java.util.List;

public interface CardSlotEquipmentService {
    CardSlotEquipment save(CardSlotEquipment cardSlotEquipment);

    CardSlotEquipment findById(Long id);

    void updateCardSlots(CardSlotEquipment cardSlotEquipment, Integer cardSlotId);

    void updateCardSlots(Player player, List<Integer> cardSlotItems);

    List<Integer> getEquippedCardSlots(Player player);
}
