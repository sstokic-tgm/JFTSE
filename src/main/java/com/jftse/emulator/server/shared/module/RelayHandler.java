package com.jftse.emulator.server.shared.module;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@Service
@Getter
@Setter
public class RelayHandler {
    private ConcurrentLinkedDeque<Client> clientList;

    @PostConstruct
    public void init() {
        clientList = new ConcurrentLinkedDeque<>();
    }

    public void addClient(Client client) {
        clientList.add(client);
    }

    public void removeClient(Client client) {
        clientList.remove(client);
    }

    public void removeClient(int index) {
        clientList.remove(index);
    }

    public List<Client> getClientsInGameSession(int sessionId) {
        return clientList.stream()
                .filter(c -> c.getActiveGameSession() != null && c.getActiveGameSession().getSessionId() == sessionId)
                .collect(Collectors.toList());
    }
}
