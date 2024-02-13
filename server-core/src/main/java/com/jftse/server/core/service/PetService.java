package com.jftse.server.core.service;

import com.jftse.entities.database.model.pet.Pet;
import com.jftse.entities.database.model.player.Player;

import java.util.List;

public interface PetService {
    List<Pet> findAllByPlayerId(Long playerId);
    void createPet(Integer itemIndex, Player player);
}
