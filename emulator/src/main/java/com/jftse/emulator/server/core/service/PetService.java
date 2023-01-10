package com.jftse.emulator.server.core.service;

import com.jftse.entities.database.model.pet.Pet;
import com.jftse.entities.database.repository.pet.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class PetService {
    private final PetRepository petRepository;

    public List<Pet> findAllByPlayerId(Long playerId) {
        return petRepository.findAllByPlayerId(playerId);
    }
}
