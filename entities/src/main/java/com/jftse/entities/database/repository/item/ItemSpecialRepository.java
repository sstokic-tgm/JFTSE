package com.jftse.entities.database.repository.item;

import com.jftse.entities.database.model.item.ItemSpecial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemSpecialRepository extends JpaRepository<ItemSpecial, Long> {
    Optional<ItemSpecial> findByItemIndex(Integer itemIndex);
}
