package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.entities.database.model.pet.Pet;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CPetRequestRoomAnswerPacket extends Packet {
    public static final byte SUCCESS = 0;
    public static final byte NO_PET_SELECTED = 1;
    public static final byte NO_PERMISSION = 2;
    public static final byte NO_FREE_SLOT = 3;
    public static final byte PET_NOT_ALLOWED = 4;
    public static final byte CAN_NOT_ADD_PET = 5;

    public S2CPetRequestRoomAnswerPacket(byte result, boolean isAdd, byte slot, Pet pet) {
        super(PacketOperations.S2CPetRequestRoomAnswer);

        this.write(result);
        this.write(isAdd);
        this.write(slot);

        if (pet != null) {
            this.write(pet.getName());
            this.write(pet.getLevel());
            this.write(pet.getType());
            this.write(pet.getHp());
            this.write(pet.getStrength());
            this.write(pet.getStamina());
            this.write(pet.getDexterity());
            this.write(pet.getWillpower());
            this.write(pet.getHunger());
            this.write(pet.getEnergy());
        }
    }
}
