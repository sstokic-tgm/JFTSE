package com.jftse.emulator.server.database.repository.item;

import com.jftse.emulator.server.database.model.item.ItemMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemMaterialRepository extends JpaRepository<ItemMaterial, Long> {
    @Query(value = "SELECT im.sellPrice FROM ItemMaterial im WHERE im.itemIndex = :itemIndex")
    List<Integer> getItemSellPriceByItemIndex(@Param("itemIndex") Integer itemIndex);
}
