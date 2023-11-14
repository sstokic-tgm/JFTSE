package com.jftse.entities.database.repository.battle;

import com.jftse.entities.database.model.battle.Guardian2Maps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface Guardian2MapsRepository extends JpaRepository<Guardian2Maps, Long> {
    @Query("SELECT g2m FROM Guardian2Maps g2m WHERE g2m.map.id = :mapId AND g2m.status.id = 1")
    List<Guardian2Maps> findAllByMap(Long mapId);

    @Query("SELECT g2m FROM Guardian2Maps g2m WHERE g2m.map.id = :mapId AND g2m.status.id = 1 AND g2m.side = :side")
    List<Guardian2Maps> findAllByMapAndSide(Long mapId, Guardian2Maps.Side side);

    @Query("SELECT g2m FROM Guardian2Maps g2m WHERE g2m.map.id = :mapId AND g2m.status.id = 1 AND g2m.scenario.id = :scenarioId")
    List<Guardian2Maps> findAllByMapAndScenario(Long mapId, Long scenarioId);

    @Query("SELECT g2m FROM Guardian2Maps g2m WHERE g2m.map.id = :mapId AND g2m.status.id = 1 AND g2m.scenario.id = :scenarioId AND g2m.side = :side")
    List<Guardian2Maps> findAllByMapAndScenarioAndSide(Long mapId, Long scenarioId, Guardian2Maps.Side side);

    @Query("SELECT g2m FROM Guardian2Maps g2m WHERE g2m.map.id = :mapId AND g2m.status.id = 1 AND g2m.guardian.id = :guardianId AND g2m.scenario.id = :scenarioId")
    List<Guardian2Maps> findAllByMapAndGuardianAndScenario(Long mapId, Long guardianId, Long scenarioId);

    @Query("SELECT g2m FROM Guardian2Maps g2m WHERE g2m.map.id = :mapId AND g2m.status.id = 1 AND g2m.bossGuardian.id = :bossGuardianId AND g2m.scenario.id = :scenarioId")
    List<Guardian2Maps> findAllByMapAndBossGuardianAndScenario(Long mapId, Long bossGuardianId, Long scenarioId);

}
