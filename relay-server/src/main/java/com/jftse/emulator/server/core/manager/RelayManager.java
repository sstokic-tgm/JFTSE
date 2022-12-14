package com.jftse.emulator.server.core.manager;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.service.BlockedIPService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
@Getter
@Setter
@Log4j2
public class RelayManager {
    private static RelayManager instance;

    @Autowired
    private BlockedIPService blockedIPService;

    private ConcurrentHashMap<Integer, ConcurrentLinkedDeque<FTClient>> clients;

    private final Object lock = new Object();

    @PostConstruct
    public void init() {
        instance = this;

        clients = new ConcurrentHashMap<>();

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static RelayManager getInstance() {
        return instance;
    }

    public void addClient(final int sessionId, final FTClient client) {
        ConcurrentLinkedDeque<FTClient> clientList;
        if (clients.containsKey(sessionId)) {
            clientList = clients.get(sessionId);
        } else {
            clientList = new ConcurrentLinkedDeque<>();
        }
        clientList.add(client);
        clients.put(sessionId, clientList);
    }

    public void removeClient(final int sessionId, final FTClient client) {
        if (clients.containsKey(sessionId)) {
            ConcurrentLinkedDeque<FTClient> clientList = clients.get(sessionId);
            clientList.remove(client);

            if (clientList.isEmpty())
                clients.remove(sessionId);
        }
    }

    public final List<FTClient> getClientsInSession(final int sessionId) {
        return new ArrayList<>(clients.get(sessionId));
    }
}
