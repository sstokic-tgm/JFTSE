package com.ft.emulator.server.database.repository.player;

import com.ft.emulator.server.database.model.player.ClothEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClothEquipmentRepository extends JpaRepository<ClothEquipment, Long> {
    // empty..
}
