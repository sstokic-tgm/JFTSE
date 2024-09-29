package com.jftse.entities.database.repository.pet;

import com.jftse.entities.database.model.pet.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {
    @Query(value = "FROM Pet WHERE player_id = :playerId")
    List<Pet> findAllByPlayerId(@Param("playerId") Long playerId);
}
