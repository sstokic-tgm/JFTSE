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
}
