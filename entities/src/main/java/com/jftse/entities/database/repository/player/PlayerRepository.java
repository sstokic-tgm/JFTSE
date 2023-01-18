package com.jftse.entities.database.repository.player;

import com.jftse.entities.database.model.player.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByName(String name);
    List<Player> findAllByName(String name);
    Page<Player> findAllByAlreadyCreatedTrue(Pageable pageable);
    List<Player> findAllByAlreadyCreatedTrue(Sort sort);
    List<Player> findAllByAccount_Id(Long accountId);

    @Query(nativeQuery = true, value = "call ranking_by_name_and_gamemode(:name, :gameMode);")
    int getRankingByNameAndGameMode(@Param("name") String name, @Param("gameMode") byte gameMode);

    @Query(nativeQuery = true, value =
            "SELECT COUNT(*) as total_entries " +
            "FROM TutorialProgress AS tp1 " +
            "JOIN " +
            "   (SELECT tp2.player_id, COUNT(*) as entries " +
            "       FROM TutorialProgress AS tp2 " +
            "       JOIN Player AS pl ON tp2.player_id = pl.id " +
            "       WHERE tp2.success = 1 AND pl.account_id = :accountId " +
            "       GROUP BY tp2.player_id " +
            "       ORDER BY entries DESC " +
            "   LIMIT 1) as most_entries " +
            "ON tp1.player_id = most_entries.player_id " +
            "WHERE tp1.success = 1;")
    int getTutorialProgressSucceededCount(@Param("accountId") Long accountId);

    @Query(value = "FROM Player p "
            + "LEFT JOIN FETCH p.account account "
            + "LEFT JOIN FETCH p.pocket pocket "
            + "LEFT JOIN FETCH p.clothEquipment clothEquipment "
            + "LEFT JOIN FETCH p.quickSlotEquipment quickSlotEquipment "
            + "LEFT JOIN FETCH p.playerStatistic playerStatistic "
            + "WHERE p.id = :playerId")
    Optional<Player> findByIdFetched(@Param("playerId") Long playerId);

    @Query(value = "FROM Player p "
            + "LEFT JOIN FETCH p.account account "
            + "LEFT JOIN FETCH p.pocket pocket "
            + "LEFT JOIN FETCH p.clothEquipment clothEquipment "
            + "LEFT JOIN FETCH p.quickSlotEquipment quickSlotEquipment "
            + "LEFT JOIN FETCH p.playerStatistic playerStatistic "
            + "WHERE p.name = :name")
    List<Player> findAllByNameFetched(@Param("name") String name);
}
