package com.jftse.emulator.server.database.repository.item;

import com.jftse.emulator.server.database.model.item.ItemHouseDeco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemHouseDecoRepository extends JpaRepository<ItemHouseDeco, Long> {
    Optional<ItemHouseDeco> findItemHouseDecoByItemIndex(Integer itemIndex);

    @Query(value = "SELECT ihd.itemIndex FROM ItemHouseDeco ihd WHERE ihd.kind = :kind")
    List<Integer> findItemIndexListByKind(@Param("kind") String kind);
}
