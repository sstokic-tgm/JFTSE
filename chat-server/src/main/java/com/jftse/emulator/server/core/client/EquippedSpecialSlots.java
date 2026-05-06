package com.jftse.emulator.server.core.client;

import com.jftse.entities.database.model.player.Player;

import java.util.List;

public record EquippedSpecialSlots(long id, int slot1, int slot2, int slot3, int slot4) {
    public static EquippedSpecialSlots of(Player player) {
        return new EquippedSpecialSlots(
                player.getSpecialSlotEquipment().getId(),
                player.getSpecialSlotEquipment().getSlot1(),
                player.getSpecialSlotEquipment().getSlot2(),
                player.getSpecialSlotEquipment().getSlot3(),
                player.getSpecialSlotEquipment().getSlot4()
        );
    }

    public static EquippedSpecialSlots of(long id, List<Integer> slots) {
        assert slots.size() == 4;
        return new EquippedSpecialSlots(id, slots.get(0), slots.get(1), slots.get(2), slots.get(3));
    }

    public List<Integer> toList() {
        return List.of(slot1, slot2, slot3, slot4);
    }

    public int hasItem(int id) {
        if (slot1 == id) return slot1;
        if (slot2 == id) return slot2;
        if (slot3 == id) return slot3;
        if (slot4 == id) return slot4;
        return 0;
    }

    public int getSlotIndex(int id) {
        if (slot1 == id) return 1;
        if (slot2 == id) return 2;
        if (slot3 == id) return 3;
        if (slot4 == id) return 4;
        return 0;
    }
}
