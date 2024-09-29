package com.jftse.entities.database.repository.item;

import com.jftse.entities.database.model.item.ItemEnchantLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemEnchantLevelRepository extends JpaRepository<ItemEnchantLevel, Long> {
    Optional<ItemEnchantLevel> findByGradeAndElementalKind(Integer grade, String elementalKind);
    List<ItemEnchantLevel> findAllByElementalKind(String elementalKind);
}
