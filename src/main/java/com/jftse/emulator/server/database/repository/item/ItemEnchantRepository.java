package com.jftse.emulator.server.database.repository.item;

import com.jftse.emulator.server.database.model.item.ItemEnchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemEnchantRepository extends JpaRepository<ItemEnchant, Long> {
    @Query(value = "SELECT ie.sellPrice FROM ItemEnchant ie WHERE ie.itemIndex = :itemIndex")
    List<Integer> getItemSellPriceByItemIndex(@Param("itemIndex") Integer itemIndex);

    @Query(value = "SELECT ie.itemIndex FROM ItemEnchant ie WHERE ie.kind = :kind")
    List<Integer> getItemIndexListByKind(@Param("kind") String kind);
}
