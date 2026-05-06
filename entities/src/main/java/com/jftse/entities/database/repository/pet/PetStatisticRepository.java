package com.jftse.entities.database.repository.pet;

import com.jftse.entities.database.model.pet.PetStatistic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetStatisticRepository extends JpaRepository<PetStatistic, Long> {
    // empty
}
