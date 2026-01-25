package com.jftse.emulator.server.core.client;

import com.jftse.entities.database.model.player.Player;

import java.util.List;

public record EquippedToolSlots(long id, int slot1, int slot2, int slot3, int slot4, int slot5) {
    public static EquippedToolSlots of(Player player) {
        return new EquippedToolSlots(
                player.getToolSlotEquipment().getId(),
                player.getToolSlotEquipment().getSlot1(),
                player.getToolSlotEquipment().getSlot2(),
                player.getToolSlotEquipment().getSlot3(),
                player.getToolSlotEquipment().getSlot4(),
                player.getToolSlotEquipment().getSlot5()
        );
    }

    public List<Integer> toList() {
        return List.of(slot1, slot2, slot3, slot4, slot5);
    }
}
