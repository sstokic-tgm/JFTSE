package com.jftse.entities.database.repository.tutorial;

import com.jftse.entities.database.model.tutorial.Tutorial;
import com.jftse.entities.database.model.tutorial.TutorialProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TutorialProgressRepository extends JpaRepository<TutorialProgress, Long> {
    @Query(value = "FROM TutorialProgress tp LEFT JOIN FETCH tp.player player LEFT JOIN FETCH tp.tutorial tutorial WHERE player_id = :playerId")
    List<TutorialProgress> findAllByPlayerIdFetched(@Param("playerId") Long playerId);

    @Query(value = "SELECT tp FROM TutorialProgress tp JOIN FETCH tp.tutorial t WHERE tp.player.id = :playerId")
    List<TutorialProgress> findAllByPlayerId(Long playerId);

    @Query(value = "SELECT tp FROM TutorialProgress tp JOIN FETCH tp.tutorial t WHERE tp.player.id = :playerId AND tp.tutorial = :tutorial")
    Optional<TutorialProgress> findByPlayerIdAndTutorial(Long playerId, Tutorial tutorial);
}
