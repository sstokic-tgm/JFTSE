package com.ft.emulator.server.shared.module;

import com.ft.emulator.server.database.model.player.Player;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Getter
@Setter
public class GameHandler {

    private List<Client> clientList;

    @PostConstruct
    public void init() {

        clientList = new ArrayList<>();
    }

    public void addClient(Client client) {
        clientList.add(client);
    }

    public void removeClient(Client client) {
        clientList.remove(client);
    }

    public List<Player> getPlayersInLobby() {

        return clientList.stream()
		.filter(Client::isInLobby)
		.map(Client::getActivePlayer)
		.collect(Collectors.toList());
    }

    public List<Client> getClientsInLobby() {

        return clientList.stream()
		.filter(Client::isInLobby)
		.collect(Collectors.toList());
    }
}