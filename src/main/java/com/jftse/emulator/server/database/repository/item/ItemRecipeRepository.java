package com.jftse.emulator.server.database.repository.item;

import com.jftse.emulator.server.database.model.item.ItemRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRecipeRepository extends JpaRepository<ItemRecipe, Long> {
    @Query(value = "SELECT ir.itemIndex FROM ItemRecipe ir WHERE ir.kind = :kind")
    List<Integer> findItemIndexListByKind(@Param("kind") String kind);

    @Query(value = "SELECT ir.itemIndex FROM ItemRecipe ir WHERE ir.kind = :kind AND ir.forPlayer = :forPlayer")
    List<Integer> findItemIndexListByKindAndForPlayer(@Param("kind") String kind, @Param("forPlayer") String forPlayer);
}
