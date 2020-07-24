package com.ft.emulator.server.database.repository.tutorial;

import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.database.model.tutorial.Tutorial;
import com.ft.emulator.server.database.model.tutorial.TutorialProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TutorialProgressRepository extends JpaRepository<TutorialProgress, Long> {

    @Query(value = "FROM TutorialProgress tp LEFT JOIN FETCH tp.player player LEFT JOIN FETCH tp.tutorial tutorial WHERE player_id = :playerId")
    List<TutorialProgress> findAllByPlayerIdFetched(Long playerId);

    Optional<TutorialProgress> findByPlayerAndTutorial(Player player, Tutorial tutorial);
}