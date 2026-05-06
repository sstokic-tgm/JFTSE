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

    @Query(
            value = "SELECT p FROM Player p JOIN FETCH p.playerStatistic ps WHERE p.alreadyCreated = true",
            countQuery = "SELECT COUNT(p) FROM Player p WHERE p.alreadyCreated = true"
    )
    Page<Player> findAllByAlreadyCreatedTrue(Pageable pageable);

    List<Player> findAllByAlreadyCreatedTrue(Sort sort);

    List<Player> findAllByAccount_Id(Long accountId);

    @Query(nativeQuery = true, value = "call ranking_by_name_and_gamemode(:name, :gameMode);")
    int getRankingByNameAndGameMode(@Param("name") String name, @Param("gameMode") byte gameMode);

    @Query(value = """
            SELECT COALESCE(MAX(x.cnt), 0)
            FROM (
                SELECT COUNT(*) AS cnt
                FROM TutorialProgress tp
                JOIN Player p ON p.id = tp.player_id
                WHERE tp.success = 1
                  AND p.account_id = :accountId
                GROUP BY tp.player_id
            ) x
            """, nativeQuery = true)
    int getTutorialProgressSucceededCount(@Param("accountId") Long accountId);

    @Query(value = """
            SELECT p FROM Player p
            JOIN FETCH p.account account
            JOIN FETCH p.pocket pocket
            JOIN FETCH p.clothEquipment clothEquipment
            JOIN FETCH p.quickSlotEquipment quickSlotEquipment
            JOIN FETCH p.toolSlotEquipment toolSlotEquipment
            JOIN FETCH p.specialSlotEquipment specialSlotEquipment
            JOIN FETCH p.cardSlotEquipment cardSlotEquipment
            JOIN FETCH p.playerStatistic playerStatistic
            WHERE p.id = :playerId
            """)
    Optional<Player> findByIdFetched(@Param("playerId") Long playerId);

    @Query(value = """
            SELECT p FROM Player p
            JOIN FETCH p.clothEquipment clothEquipment
            JOIN FETCH p.quickSlotEquipment quickSlotEquipment
            JOIN FETCH p.toolSlotEquipment toolSlotEquipment
            JOIN FETCH p.specialSlotEquipment specialSlotEquipment
            JOIN FETCH p.cardSlotEquipment cardSlotEquipment
            WHERE p.id = :playerId
            """)
    Optional<Player> findWithEquipmentById(Long playerId);

    @Query(value = "SELECT p FROM Player p JOIN FETCH p.account account WHERE p.id = :playerId")
    Optional<Player> findWithAccountById(Long playerId);

    @Query(value = "SELECT p FROM Player p JOIN FETCH p.pocket pocket WHERE p.id = :playerId")
    Optional<Player> findWithPocketById(Long playerId);

    @Query(value = "SELECT p FROM Player p JOIN FETCH p.playerStatistic playerStatistic WHERE p.id = :playerId")
    Optional<Player> findWithStatisticById(Long playerId);

    @Query("SELECT p FROM Player p JOIN FETCH p.clothEquipment ce WHERE p.account.id = :accountId")
    List<Player> getPlayerListByAccountId(Long accountId);

    @Query(value = """
            SELECT p FROM Player p
            JOIN FETCH p.account account
            JOIN FETCH p.pocket pocket
            JOIN FETCH p.clothEquipment clothEquipment
            JOIN FETCH p.quickSlotEquipment quickSlotEquipment
            JOIN FETCH p.toolSlotEquipment toolSlotEquipment
            JOIN FETCH p.specialSlotEquipment specialSlotEquipment
            JOIN FETCH p.cardSlotEquipment cardSlotEquipment
            JOIN FETCH p.playerStatistic playerStatistic
            WHERE p.name = :name
            """)
    List<Player> findAllByNameFetched(@Param("name") String name);
}
