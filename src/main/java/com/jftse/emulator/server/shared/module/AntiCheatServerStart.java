package com.jftse.emulator.server.shared.module;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.listener.AntiCheatHeartBeatNetworkListener;
import com.jftse.emulator.server.core.manager.ServerManager;
import com.jftse.emulator.server.networking.Server;
import com.jftse.emulator.server.networking.ThreadedConnectionListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.Executors;

@Log4j2
@Component
@Order(4)
public class AntiCheatServerStart implements CommandLineRunner {
    @Autowired
    private AntiCheatHeartBeatNetworkListener antiCheatHeartBeatNetworkListener;

    @Autowired
    private ServerManager serverManager;
    @Autowired
    private ConfigService configService;

    @Override
    public void run(String... args) throws Exception {
        if (configService.getValue("anticheat.enabled", false)) {
            log.info("Initializing anti cheat heartbeat server...");

            Server antiCheatServer = new Server();
            antiCheatServer.addListener(new ThreadedConnectionListener(antiCheatHeartBeatNetworkListener, Executors.newFixedThreadPool(4)));
            try {
                antiCheatServer.bind(configService.getValue("anticheat.port", 1337)); // adjustable
            }
            catch (IOException ioe) {
                log.error("Failed to start anti cheat heartbeat server!");
                ioe.printStackTrace();
                System.exit(1);
            }
            antiCheatServer.start("anti cheat server");

            log.info("Successfully initialized");
            log.info("--------------------------------------");

            serverManager.add(antiCheatServer);
        }
    }
}
