package com.jftse.emulator.server.core.client;

import com.jftse.entities.database.model.player.Player;

import java.util.List;

public record EquippedCardSlots(long id, int slot1, int slot2, int slot3, int slot4) {
    public static EquippedCardSlots of(Player player) {
        return new EquippedCardSlots(
                player.getCardSlotEquipment().getId(),
                player.getCardSlotEquipment().getSlot1(),
                player.getCardSlotEquipment().getSlot2(),
                player.getCardSlotEquipment().getSlot3(),
                player.getCardSlotEquipment().getSlot4()
        );
    }

    public List<Integer> toList() {
        return List.of(slot1, slot2, slot3, slot4);
    }
}
