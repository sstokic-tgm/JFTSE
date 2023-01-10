package com.jftse.server.core.service;

import com.jftse.entities.database.model.pet.Pet;

import java.util.List;

public interface PetService {
    List<Pet> findAllByPlayerId(Long playerId);
}
