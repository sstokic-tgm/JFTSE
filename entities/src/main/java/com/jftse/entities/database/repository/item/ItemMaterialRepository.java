package com.jftse.entities.database.repository.item;

import com.jftse.entities.database.model.item.ItemMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemMaterialRepository extends JpaRepository<ItemMaterial, Long> {
    @Query(value = "SELECT im.sellPrice FROM ItemMaterial im WHERE im.itemIndex = :itemIndex")
    List<Integer> getItemSellPriceByItemIndex(@Param("itemIndex") Integer itemIndex);

    @Query(value = "SELECT im.itemIndex FROM ItemMaterial im")
    List<Integer> findAllItemIndexes();

    @Query(value = "SELECT im.itemIndex FROM ItemMaterial im WHERE name IN :nameList")
    List<Integer> findAllItemIndexesByNames(@Param("nameList") List<String> materialNames);

    Optional<ItemMaterial> findByItemIndex(Integer itemIndex);
}
