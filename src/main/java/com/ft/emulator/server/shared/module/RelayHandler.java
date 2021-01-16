package com.ft.emulator.server.shared.module;

import com.ft.emulator.server.game.core.matchplay.room.GameSession;
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
public class RelayHandler {
    private List<Client> clientList;
    private List<GameSession> sessionList;

    @PostConstruct
    public void init() {
        clientList = new ArrayList<>();
        sessionList = new ArrayList<>();
    }

    public void addClient(Client client) {
        clientList.add(client);
    }

    public void removeClient(Client client) {
        clientList.remove(client);
    }

    public List<Client> getClientsInGameSession(Client client) {
        return clientList.stream()
                .filter(c -> c.getActiveGameSession() != null
                          && c.getActiveGameSession().getSessionId() == client.getActiveGameSession().getSessionId())
                .collect(Collectors.toList());
    }
}
