package com.ft.emulator.server.shared.module.checker;

import com.ft.emulator.server.networking.Server;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class RelayServerChecker extends ServerChecker implements Runnable {
    private final Server server;

    public RelayServerChecker(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        boolean isAlive = this.isAlive("0.0.0.0", 5896);
        log.info("relay server " + (isAlive ? "is" : "is not") + " online.");

        if (!isAlive) {
            log.info("Trying to restart the relay server...");

            try {
                server.dispose();
                server.restart();
                server.bind(5896);
            }
            catch (IOException ioe) {
                log.error("Failed to start relay server!", ioe);
            }
            server.start("relay server");

            log.info("Relay server has been restarted.");
        }
    }
}