package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.pet.Pet;
import com.jftse.entities.database.model.pet.PetStatistic;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.pet.PetRepository;
import com.jftse.entities.database.repository.pet.PetStatisticRepository;
import com.jftse.server.core.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class PetServiceImpl implements PetService {
    private final PetRepository petRepository;
    private final PetStatisticRepository petStatisticRepository;

    @Override
    public List<Pet> findAllByPlayerId(Long playerId) {
        return petRepository.findAllByPlayerId(playerId);
    }

    @Override
    public void createPet(Integer itemIndex, Player player) {
        PetStatistic petStatistic = new PetStatistic();
        petStatistic = petStatisticRepository.save(petStatistic);

        switch (itemIndex) {
            case 1:
                createPet("Pikaro", 0, 0, 0, 0, 180, 50, 100, 30, 60, 1, 0, petStatistic, player);
                break;
            case 2:
                createPet("Poteko", 0, 0, 0, 0, 200, 100, 150, 60, 120, 1, 1, petStatistic, player);
                break;
            case 3:
                createPet("Boonga", 0, 0, 0, 0, 200, 100, 150, 60, 120, 1, 2, petStatistic, player);
                break;
            case 4:
                createPet("Goliath", 0, 0, 0, 0, 280, 100, 150, 60, 120, 1, 3, petStatistic, player);
                break;
            case 5:
                createPet("Blood", 0, 0, 0, 0, 200, 100, 150, 60, 120, 1, 4, petStatistic, player);
                break;
            case 6:
                createPet("Goddess", 0, 0, 0, 0, 200, 100, 150, 60, 120, 1, 5, petStatistic, player);
                break;
            case 7:
                createPet("Lizard", 0, 0, 0, 0, 200, 100, 150, 60, 120, 1, 6, petStatistic, player);
                break;
            case 8:
                createPet("Tossakan", 0, 0, 0, 0, 280, 100, 150, 60, 120, 1, 7, petStatistic, player);
                break;
            case 9:
                createPet("Ninkaro", 0, 0, 0, 0, 280, 100, 150, 60, 120, 1, 8, petStatistic, player);
                break;
            default:
                break;
        }
    }

    private void createPet(String nameLabel, int strength, int stamina, int dexterity, int willpower,
                           int hp, int energy, int hunger, int life, int lifeMax, int level, int model,
                           PetStatistic petStatistic, Player player) {
        Pet pet = new Pet();
        pet.setPetStatistic(petStatistic);
        pet.setPlayer(player);
        pet.setName(nameLabel);
        pet.setType((byte) model);
        pet.setLevel((byte) level);
        pet.setExpPoints(0);
        pet.setHp(hp);
        pet.setStrength((byte) strength);
        pet.setStamina((byte) stamina);
        pet.setDexterity((byte) dexterity);
        pet.setWillpower((byte) willpower);
        pet.setHunger(hunger);
        pet.setEnergy(energy);
        pet.setLifeMax(lifeMax);
        pet.setAlive(true);
        pet.setValidUntil(calculateValidUntil(life));

        petRepository.save(pet);
    }

    private Date calculateValidUntil(int life) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, life);
        return calendar.getTime();
    }
}
