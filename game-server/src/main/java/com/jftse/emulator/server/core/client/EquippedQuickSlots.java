package com.jftse.emulator.server.core.client;

import com.jftse.entities.database.model.player.Player;

import java.util.List;

public record EquippedQuickSlots(long id, int slot1, int slot2, int slot3, int slot4, int slot5) {
    public static EquippedQuickSlots of(Player player) {
        return new EquippedQuickSlots(
                player.getQuickSlotEquipment().getId(),
                player.getQuickSlotEquipment().getSlot1(),
                player.getQuickSlotEquipment().getSlot2(),
                player.getQuickSlotEquipment().getSlot3(),
                player.getQuickSlotEquipment().getSlot4(),
                player.getQuickSlotEquipment().getSlot5()
        );
    }

    public static EquippedQuickSlots of(long id, List<Integer> slots) {
        assert slots.size() == 5;
        return new EquippedQuickSlots(id, slots.get(0), slots.get(1), slots.get(2), slots.get(3), slots.get(4));
    }

    public List<Integer> toList() {
        return List.of(slot1, slot2, slot3, slot4, slot5);
    }

    public int hasItem(int id) {
        if (slot1 == id) return slot1;
        if (slot2 == id) return slot2;
        if (slot3 == id) return slot3;
        if (slot4 == id) return slot4;
        if (slot5 == id) return slot5;
        return 0;
    }

    public int getSlotIndex(int id) {
        if (slot1 == id) return 1;
        if (slot2 == id) return 2;
        if (slot3 == id) return 3;
        if (slot4 == id) return 4;
        if (slot5 == id) return 5;
        return 0;
    }
}
