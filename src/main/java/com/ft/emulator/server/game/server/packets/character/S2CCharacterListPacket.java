package com.ft.emulator.server.game.server.packets.character;

import com.ft.emulator.server.database.model.account.Account;
import com.ft.emulator.server.database.model.character.CharacterPlayer;
import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

import java.util.List;

public class S2CCharacterListPacket extends Packet {

    public S2CCharacterListPacket(Account account, List<CharacterPlayer> characterPlayerList) {

        super(PacketID.S2CCharacterList);

        this.write(0);
        this.write(0);
        this.write(Math.toIntExact(account.getId()));
        this.write((byte)0);
        this.write(account.getGameMaster()); // GM

        if(characterPlayerList != null) {
            this.write((byte) characterPlayerList.size());

            for(CharacterPlayer characterPlayer : characterPlayerList) {

                this.write(Math.toIntExact(characterPlayer.getId()));
                this.write(characterPlayer.getName());
                this.write((short)0);
                this.write(characterPlayer.getLevel());
                this.write(characterPlayer.getAlreadyCreated());
                this.write(!characterPlayer.getFirstCharacter()); // forCharacter delete: true/false
                this.write(characterPlayer.getGold());
		this.write(characterPlayer.getCType());
		this.write(characterPlayer.getStrength());
		this.write(characterPlayer.getStamina());
		this.write(characterPlayer.getDexterity());
		this.write(characterPlayer.getWillpower());
		this.write(characterPlayer.getStatusPoints());
		this.write(characterPlayer.getNameChangeAllowed()); // old, "Change Nickname"
		this.write(characterPlayer.getNameChangeAllowed()); // new, change nickname item/icon
		this.write(characterPlayer.getHair());
		this.write(characterPlayer.getFace());
		this.write(characterPlayer.getDress());
		this.write(characterPlayer.getPants());
		this.write(characterPlayer.getSocks());
		this.write(characterPlayer.getShoes());
		this.write(characterPlayer.getGloves());
		this.write(characterPlayer.getRacket());
		this.write(characterPlayer.getGlasses());
		this.write(characterPlayer.getBag());
		this.write(characterPlayer.getHat());
		this.write(characterPlayer.getDye());
	    }
	}
    }
}