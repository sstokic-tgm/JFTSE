package com.ft.emulator.server.database.repository.player;

import com.ft.emulator.server.database.model.player.QuickSlotEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuickSlotEquipmentRepository extends JpaRepository<QuickSlotEquipment, Long> {
    // empty..
}
