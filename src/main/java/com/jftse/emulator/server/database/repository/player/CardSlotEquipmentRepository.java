package com.jftse.emulator.server.database.repository.player;

import com.jftse.emulator.server.database.model.player.CardSlotEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardSlotEquipmentRepository extends JpaRepository<CardSlotEquipment, Long> {
    // empty..
}
