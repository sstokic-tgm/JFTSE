package com.jftse.emulator.server.shared.module;

import com.jftse.emulator.common.GlobalSettings;
import com.jftse.emulator.server.game.core.anticheat.AntiCheatHeartBeatNetworkListener;
import com.jftse.emulator.server.networking.Server;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Log4j2
@Component
@Order(4)
public class AntiCheatServerStart implements CommandLineRunner {
    @Autowired
    private AntiCheatHeartBeatNetworkListener antiCheatHeartBeatNetworkListener;

    @Override
    public void run(String... args) throws Exception {
        if (GlobalSettings.IsAntiCheatEnabled) {
            log.info("Initializing anti cheat heartbeat server...");

            antiCheatHeartBeatNetworkListener = new AntiCheatHeartBeatNetworkListener();
            Server antiCheatServer = new Server();
            antiCheatServer.addListener(antiCheatHeartBeatNetworkListener);

            try {
                antiCheatServer.bind(1337); // adjustable
            }
            catch (IOException ioe) {
                log.error("Failed to start anti cheat heartbeat server!");
                ioe.printStackTrace();
                System.exit(1);
            }
            antiCheatServer.start("anti cheat server");

            log.info("Successfully initialized!");
            log.info("--------------------------------------");
        }
    }
}
