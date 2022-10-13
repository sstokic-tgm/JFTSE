package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.pet.Pet;
import com.jftse.entities.database.repository.pet.PetRepository;
import com.jftse.server.core.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class PetServiceImpl implements PetService {
    private final PetRepository petRepository;

    @Override
    public List<Pet> findAllByPlayerId(Long playerId) {
        return petRepository.findAllByPlayerId(playerId);
    }
}
