package com.jftse.emulator.server.core.manager;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
@Getter
@Setter
@Log4j2
public class AuthenticationManager {
    private static AuthenticationManager instance;

    private ConcurrentLinkedDeque<FTClient> clients;

    @PostConstruct
    public void init() {
        instance = this;

        clients = new ConcurrentLinkedDeque<>();

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public void onExit() {
        log.info("Closing all connections");
        for (FTClient client : clients) {
            client.getConnection().close();
        }
        log.info("All connections closed");

        clients.clear();

        log.info("AuthenticationManager stopped");
    }

    public static AuthenticationManager getInstance() {
        return instance;
    }

    public void addClient(FTClient client) {
        clients.add(client);
    }

    public void removeClient(FTClient client) {
        clients.remove(client);
    }
}
