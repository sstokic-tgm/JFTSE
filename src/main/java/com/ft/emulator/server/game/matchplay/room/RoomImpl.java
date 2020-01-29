package com.ft.emulator.server.game.matchplay.room;

import com.ft.emulator.server.shared.module.Client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RoomImpl {

    public RoomImpl() {}

    public boolean removePlayerFromRoom(Client client, List<Room> roomList) {

        boolean roomChanges = false;

	if(client.getActiveRoom() != null) {

	    roomList.forEach(r -> {

	        if(r.getId() == client.getActiveRoom().getId()) {

	            List<RoomPlayer> toRemove = r.getPlayerList().stream()
			    .filter(rp -> rp.getPlayer() == client.getActiveCharacterPlayer())
			    .collect(Collectors.toList());

	            r.getPlayerList().removeAll(toRemove);
		}
	    });

	    removeRoom(roomList);

	    client.setActiveRoom(null);

	    roomChanges = true;
	}

	return roomChanges;
    }

    private void removeRoom(List<Room> roomList) {

	roomList.removeIf(r -> r.getPlayerList().isEmpty());
    }
}