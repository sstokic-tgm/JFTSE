package com.jftse.emulator.server.database.repository.pocket;

import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.database.model.pocket.Pocket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerPocketRepository extends JpaRepository<PlayerPocket, Long> {
    Optional<PlayerPocket> findById(Long id);
    Optional<PlayerPocket> findByItemIndex(Integer itemIndex);
    Optional<PlayerPocket> findByIdAndPocket(Long id, Pocket pocket);
    Optional<PlayerPocket> findByItemIndexAndPocket(Integer itemIndex, Pocket pocket);
    List<PlayerPocket> findAllByItemIndexAndPocket(Integer itemIndex, Pocket pocket);
    Optional<PlayerPocket> findByItemIndexAndCategoryAndPocket(Integer itemIndex, String category, Pocket pocket);
    List<PlayerPocket> findAllByItemIndexAndCategoryAndPocket(Integer itemIndex, String category, Pocket pocket);
    List<PlayerPocket> findAllByPocket(Pocket pocket);
}
