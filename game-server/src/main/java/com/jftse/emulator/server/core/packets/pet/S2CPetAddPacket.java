package com.jftse.emulator.server.core.packets.pet;

import com.jftse.entities.database.model.pet.Pet;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CPetAddPacket extends Packet {
    public S2CPetAddPacket(Pet pet) {
        super(PacketOperations.S2CPetAdd);

        this.write(pet.getType(),
                pet.getName(),
                pet.getLevel(),
                pet.getExpPoints(),
                pet.getHp(),
                pet.getStrength(),
                pet.getStamina(),
                pet.getDexterity(),
                pet.getWillpower(),
                pet.getHunger(),
                pet.getEnergy(),
                pet.getLifeMax(),
                pet.getValidUntil());
    }
}
