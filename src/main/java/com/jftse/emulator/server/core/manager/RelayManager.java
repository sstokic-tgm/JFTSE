package com.jftse.emulator.server.core.manager;

import com.jftse.emulator.server.shared.module.Client;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@Service
@Getter
@Setter
@Log4j2
public class RelayManager {
    private static RelayManager instance;

    private ConcurrentLinkedDeque<Client> clientList;

    @PostConstruct
    public void init() {
        instance = this;
        clientList = new ConcurrentLinkedDeque<>();

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static RelayManager getInstance() {
        return instance;
    }

    public void addClient(Client client) {
        clientList.add(client);
    }

    public void removeClient(Client client) {
        clientList.remove(client);
    }

    public ArrayList<Client> getClientsInGameSession(int sessionId) {
        return clientList.stream()
                .filter(c -> c.getActiveGameSession() != null && c.getActiveGameSession().getSessionId() == sessionId)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
