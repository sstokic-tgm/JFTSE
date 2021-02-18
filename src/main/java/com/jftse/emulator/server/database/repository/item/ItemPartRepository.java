package com.jftse.emulator.server.database.repository.item;

import com.jftse.emulator.server.database.model.item.ItemPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemPartRepository extends JpaRepository<ItemPart, Long> {
    List<ItemPart> findByItemIndexIn(List<Integer> itemIndexList);

    @Query(value = "SELECT ip.itemIndex FROM ItemPart ip WHERE ip.forPlayer = :forPlayer AND ip.part IN :part")
    List<Integer> findItemIndexListByForPlayerAndPartIn(@Param("forPlayer") String forPlayer, @Param("part") List<String> part);

    @Query(value = "SELECT ip.itemIndex FROM ItemPart ip WHERE ip.forPlayer = :forPlayer")
    List<Integer> findItemIndexListByForPlayer(@Param("forPlayer") String forPlayer);
}
