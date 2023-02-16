package com.jftse.emulator.server.core.packets.pet;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.entities.database.model.pet.Pet;

import java.util.List;

public class S2CPetDataAnswerPacket extends Packet {
    public S2CPetDataAnswerPacket(List<Pet> petList) {
        super(PacketOperations.S2CPetDataAnswer);

        this.write((byte)petList.size());

        petList.forEach(
                pet ->
                        this.write(
                                pet.getType(),
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
                                pet.getValidUntil(),
                                pet.getAlive(),
                                pet.getPetStatistic().getBasicRecordWin(),
                                pet.getPetStatistic().getBasicRecordLoss(),
                                pet.getPetStatistic().getBattleRecordWin(),
                                pet.getPetStatistic().getBattleRecordLoss(),
                                pet.getPetStatistic().getConsecutiveWins(),
                                0, // ?
                                pet.getPetStatistic().getTotalGames(),
                                pet.getPetStatistic().getNumberOfDisconnects(),
                                0, // ?
                                0) // ?
        );
    }
}
