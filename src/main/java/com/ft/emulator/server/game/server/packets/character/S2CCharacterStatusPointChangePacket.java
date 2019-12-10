package com.ft.emulator.server.game.server.packets.character;

import com.ft.emulator.server.database.model.character.CharacterPlayer;
import com.ft.emulator.server.database.model.character.StatusPointsAddedDto;
import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CCharacterStatusPointChangePacket extends Packet {

    /**
     * TODO: reverse this packet structure correctly
     */
    public S2CCharacterStatusPointChangePacket(CharacterPlayer characterPlayer, StatusPointsAddedDto statusPointsAddedDto) {

        super(PacketID.S2CCharacterStatusPointChange);

        this.write(200); // hp

        // status points
        this.write(characterPlayer.getStrength());
        this.write(characterPlayer.getStamina());
        this.write(characterPlayer.getDexterity());
        this.write(characterPlayer.getWillpower());
        // cloth added status points
        this.write(statusPointsAddedDto.getStrength());
        this.write(statusPointsAddedDto.getStamina());
        this.write(statusPointsAddedDto.getDexterity());
        this.write(statusPointsAddedDto.getWillpower());
        // ??
        for (int i = 5; i < 13; i++) {
            this.write((byte) 0);
        }
        // ??
        this.write((byte) 0);
        this.write((byte) 0);
        // add hp
        this.write(0);
        // cloth added status points for shop
        this.write((byte) 0);
        this.write((byte) 0);
        this.write((byte) 0);
        this.write((byte) 0);
        //??
        this.write(0);
        this.write((byte) 0);
        this.write((byte) 0);
        this.write((byte) 0);
        this.write((byte) 0);
        // ??
        for (int i = 5; i < 13; i++) {
            this.write((byte) 0);
        }
        // ??
        for (int i = 5; i < 13; i++) {
            this.write((byte) 0);
        }

        this.write(characterPlayer.getStatusPoints());
    }
}