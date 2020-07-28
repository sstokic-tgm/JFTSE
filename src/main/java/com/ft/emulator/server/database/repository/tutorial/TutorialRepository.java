package com.ft.emulator.server.database.repository.tutorial;

import com.ft.emulator.server.database.model.tutorial.Tutorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
    Optional<Tutorial> findByTutorialIndex(Integer tutorialIndex);
}
