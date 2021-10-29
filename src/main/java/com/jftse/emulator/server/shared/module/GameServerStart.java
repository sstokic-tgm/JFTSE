package com.jftse.emulator.server.shared.module;

import com.jftse.emulator.server.core.game.GameServerNetworkListener;
import com.jftse.emulator.server.core.listener.RelayServerNetworkListener;
import com.jftse.emulator.server.core.manager.ThreadManager;
import com.jftse.emulator.server.networking.Server;
import com.jftse.emulator.server.networking.ThreadedConnectionListener;
import com.jftse.emulator.server.shared.module.checker.GameServerChecker;
import com.jftse.emulator.server.shared.module.checker.RelayServerChecker;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component
@Order(3)
public class GameServerStart implements CommandLineRunner {
    @Autowired
    private GameServerNetworkListener gameServerNetworkListener;
    @Autowired
    private RelayServerNetworkListener relayServerNetworkListener;

    @Autowired
    private ThreadManager threadManager;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing game server...");

        Server gameServer = new Server();
        gameServer.addListener(new ThreadedConnectionListener(gameServerNetworkListener, Executors.newFixedThreadPool(8)));
        try {
            gameServer.bind(5895);
        }
        catch (IOException ioe) {
            log.error("Failed to start game server!");
            ioe.printStackTrace();
            System.exit(1);
        }
        gameServer.start("game server");

        log.info("Successfully initialized");
        log.info("--------------------------------------");

        log.info("Initializing relay server...");

        Server relayServer = new Server();
        relayServer.addListener(new ThreadedConnectionListener(relayServerNetworkListener, Executors.newFixedThreadPool(8)));
        try {
            relayServer.bind(5896);
        }
        catch (IOException ioe) {
            log.error("Failed to start relay server!");
            ioe.printStackTrace();
            System.exit(1);
        }
        relayServer.start("relay server");

        log.info("Successfully initialized");
        log.info("--------------------------------------");

        threadManager.schedule(new GameServerChecker(gameServer, relayServer, gameServerNetworkListener, relayServerNetworkListener), 1, 2, TimeUnit.MINUTES);
        threadManager.schedule(new RelayServerChecker(relayServer, relayServerNetworkListener), 1, 5, TimeUnit.MINUTES);
    }
}
