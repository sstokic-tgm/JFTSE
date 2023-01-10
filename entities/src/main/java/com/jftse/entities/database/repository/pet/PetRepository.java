package com.jftse.entities.database.repository.pet;

import com.jftse.entities.database.model.pet.Pet;
import com.jftse.entities.database.model.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {
    @Query(value = "FROM Pet WHERE player_id = :playerId")
    List<Pet> findAllByPlayerId(@Param("playerId") Long playerId);
}
