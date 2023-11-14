package com.jftse.entities.database.repository.scenario;

import com.jftse.entities.database.model.scenario.MScenarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ScenariosRepository extends JpaRepository<MScenarios, Long> {
    List<MScenarios> findAllByComponentOf(MScenarios componentOf);
    @Query("SELECT s FROM MScenarios s WHERE s.isDefault = :isDefault AND s.gameMode = :gameMode AND s.status.id = 1")
    List<MScenarios> findAllByIsDefaultAndGameMode(Boolean isDefault, MScenarios.GameMode gameMode);
    Optional<MScenarios> findByIdAndIsDefault(Long id, Boolean isDefault);
}
