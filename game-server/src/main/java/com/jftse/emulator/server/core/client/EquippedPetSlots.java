package com.jftse.emulator.server.core.client;

import java.util.List;

public record EquippedPetSlots(long id, int slot1, int slot2) {
    public static EquippedPetSlots defaultSlots() {
        return new EquippedPetSlots(0L, 0, 0);
    }

    public List<Integer> toList() {
        return List.of(slot1, slot2);
    }
}
