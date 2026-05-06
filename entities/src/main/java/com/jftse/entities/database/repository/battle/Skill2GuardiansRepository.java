package com.jftse.entities.database.repository.battle;

import com.jftse.entities.database.model.battle.Guardian2Maps;
import com.jftse.entities.database.model.battle.Skill2Guardians;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface Skill2GuardiansRepository extends JpaRepository<Skill2Guardians, Long> {
    @Query("SELECT s2g FROM Skill2Guardians s2g WHERE s2g.guardian2Maps = :guardian2MapsId AND s2g.status.id = 1")
    List<Skill2Guardians> findAllByGuardian(Guardian2Maps guardian2MapsId);

    @Query("SELECT s2g FROM Skill2Guardians s2g WHERE s2g.btItemID = :btItemId AND s2g.status.id = 1")
    List<Skill2Guardians> findAllByBtItemId(Integer btItemId);

    @Query("SELECT s2g FROM Skill2Guardians s2g LEFT JOIN FETCH s2g.guardian2Maps g2m WHERE g2m IS NOT NULL AND g2m.map.id = :mapId AND g2m.guardian.id = :guardianId AND g2m.scenario.id = :scenarioId AND s2g.status.id = 1")
    List<Skill2Guardians> findAllByMapAndGuardianAndScenario(Long mapId, Long guardianId, Long scenarioId);

    @Query("SELECT s2g FROM Skill2Guardians s2g LEFT JOIN FETCH s2g.guardian2Maps g2m WHERE g2m IS NOT NULL AND g2m.map.id = :mapId AND g2m.bossGuardian.id = :guardianId AND g2m.scenario.id = :scenarioId AND s2g.status.id = 1")
    List<Skill2Guardians> findAllByMapAndBossGuardianAndScenario(Long mapId, Long guardianId, Long scenarioId);
}
