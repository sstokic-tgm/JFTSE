package com.ft.emulator.server.game.matchplay.room;

import com.ft.emulator.server.shared.module.Client;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RoomImpl {

    public RoomImpl() {}

    public boolean removePlayerFromRoom(Client client, List<Room> roomList) {

        boolean roomChanges = false;

	if(client.getActiveRoom() != null) {

	    roomList.stream()
		    .filter(r -> r.getId() == client.getActiveRoom().getId())
		    .map(Room::getPlayerList)
		    .flatMap(Collection::stream)
		    .collect(Collectors.toList())
		    .removeIf(rp -> rp.getPlayer() == client.getActiveCharacterPlayer());

	    removeRoom(roomList);

	    client.setActiveRoom(null);

	    roomChanges = true;
	}

	return roomChanges;
    }

    public void removeRoom(List<Room> roomList) {

	roomList.removeIf(r -> r.getPlayerList().isEmpty());
    }
}