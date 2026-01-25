package com.jftse.server.core.item;

import com.jftse.entities.database.model.pet.Pet;

public record PetCreated(Pet pet) implements AddItemHook {
}
