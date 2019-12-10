package com.ft.emulator.server.game.characterplayer;

import com.ft.emulator.common.service.Service;
import com.ft.emulator.server.database.model.character.CharacterPlayer;
import com.ft.emulator.server.game.server.packets.character.C2SCharacterStatusPointChangePacket;

import javax.persistence.EntityManagerFactory;

public class StatusPointImpl extends Service {

    public StatusPointImpl(EntityManagerFactory entityManagerFactory) {

        super(entityManagerFactory);
    }

    public boolean isStatusPointHack(C2SCharacterStatusPointChangePacket characterStatusPointChangeRequestPacket, CharacterPlayer characterPlayer) {

	// checking them so we are not 'hacked'
	byte serverStatusPoints = characterPlayer.getStatusPoints();
	byte clientStatusPoints = characterStatusPointChangeRequestPacket.getStatusPoints();

	byte strength = (byte)(characterStatusPointChangeRequestPacket.getStrength() - characterPlayer.getStrength());
	byte stamina = (byte)(characterStatusPointChangeRequestPacket.getStamina() - characterPlayer.getStamina());
	byte dexterity = (byte)(characterStatusPointChangeRequestPacket.getDexterity() - characterPlayer.getDexterity());
	byte willpower = (byte)(characterStatusPointChangeRequestPacket.getWillpower() - characterPlayer.getWillpower());

	byte newStatusPoints = (byte)(strength + stamina + dexterity + willpower + clientStatusPoints);

	return (serverStatusPoints - newStatusPoints) != 0;
    }
}