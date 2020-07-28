package com.ft.emulator.server.database.repository.item;

import com.ft.emulator.server.database.model.item.ItemTool;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemToolRepository extends JpaRepository<ItemTool, Long> {
    // empty..
}
