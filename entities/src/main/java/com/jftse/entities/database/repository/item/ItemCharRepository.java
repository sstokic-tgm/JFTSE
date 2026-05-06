package com.jftse.entities.database.repository.item;

import com.jftse.entities.database.model.item.ItemChar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemCharRepository extends JpaRepository<ItemChar, Long> {
    Optional<ItemChar> findByPlayerType(byte playerType);
}