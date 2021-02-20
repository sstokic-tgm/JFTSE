package com.jftse.emulator.server.database.repository.item;

import com.jftse.emulator.server.database.model.item.ItemHouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemHouseRepository extends JpaRepository<ItemHouse, Long> {
    Optional<ItemHouse> findItemHouseByLevel(Byte level);

    Optional<ItemHouse> findItemHouseByItemIndex(Integer itemIndex);
}
